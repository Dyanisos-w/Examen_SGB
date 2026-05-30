USE PadelDB;
GO

-- ===========================================================================
-- TRIGGER 1 : Refuser un 5e joueur sur une réservation (max 4)
-- ===========================================================================
CREATE OR ALTER TRIGGER trg_ru_MaxJoueurs
ON reservation_utilisateur
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM (
            SELECT ru.reservationid, COUNT(*) AS nb
            FROM reservation_utilisateur ru
            WHERE ru.reservationid IN (SELECT reservationid FROM INSERTED)
            GROUP BY ru.reservationid
        ) AS cnt
        WHERE cnt.nb > 4
    )
BEGIN
        RAISERROR('Une réservation ne peut pas dépasser 4 joueurs.', 16, 1);
ROLLBACK TRANSACTION;
RETURN;
END
END;
GO

-- ===========================================================================
-- TRIGGER 2 : Mettre le statut à FULL / OPEN / PRIVATE quand un joueur s'inscrit
-- ===========================================================================
CREATE OR ALTER TRIGGER trg_ru_StatutInsert
ON reservation_utilisateur
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

UPDATE r
SET r.statut = CASE
                   WHEN cnt.nb >= 4 THEN 'FULL'
                   WHEN r.type_reservation = 'PUBLIC' THEN 'OPEN'
                   ELSE 'PRIVATE'
    END
    FROM reservation r
    INNER JOIN (
        SELECT ru.reservationid, COUNT(*) AS nb
        FROM reservation_utilisateur ru
        WHERE ru.reservationid IN (SELECT reservationid FROM INSERTED)
        GROUP BY ru.reservationid
    ) AS cnt ON r.idreservation = cnt.reservationid
WHERE r.statut <> 'CANCELLED';
END;
GO

-- trg_ru_MaxJoueurs doit s'exécuter en premier
EXEC sp_settriggerorder
    @triggername = 'dbo.trg_ru_MaxJoueurs',
    @order       = 'First',
    @stmttype    = 'INSERT';

EXEC sp_settriggerorder
    @triggername = 'dbo.trg_ru_StatutInsert',
    @order       = 'Last',
    @stmttype    = 'INSERT';
GO

-- ===========================================================================
-- TRIGGER 3 : Remettre le statut à OPEN / PRIVATE / CANCELLED quand un joueur part
-- ===========================================================================
CREATE OR ALTER TRIGGER trg_ru_StatutDelete
ON reservation_utilisateur
AFTER DELETE
AS
BEGIN
    SET NOCOUNT ON;

UPDATE r
SET r.statut = CASE
                   WHEN cnt.nb = 0 THEN 'CANCELLED'
                   WHEN r.type_reservation = 'PUBLIC' THEN 'OPEN'
                   ELSE 'PRIVATE'
    END
    FROM reservation r
    INNER JOIN (
        SELECT d.reservationid, COUNT(ru.reservationid) AS nb
        FROM (SELECT DISTINCT reservationid FROM DELETED) AS d
        LEFT JOIN reservation_utilisateur ru 
            ON ru.reservationid = d.reservationid
        GROUP BY d.reservationid
    ) AS cnt ON r.idreservation = cnt.reservationid
WHERE r.statut <> 'CANCELLED';
END;
GO

-- ===========================================================================
-- TRIGGER 4 : Passer est_maintenu = 1 quand les 4 joueurs ont payé
-- ===========================================================================
CREATE OR ALTER TRIGGER trg_payment_EstMaintenu
ON payment
AFTER INSERT, UPDATE
                                  AS
BEGIN
    SET NOCOUNT ON;

UPDATE r
SET r.est_maintenu = 1
    FROM reservation r
WHERE r.idreservation IN (
    SELECT DISTINCT reservationidreservation
    FROM INSERTED
    )
  AND r.est_maintenu = 0
  AND (
    SELECT COUNT(*)
    FROM payment p
    WHERE p.reservationidreservation = r.idreservation
  AND p.statut_paiement = 'PAYE'
    ) >= 4;
END;
GO

-- ===========================================================================
-- TRIGGER 5 : Effacer la dette de pénalité d'un joueur quand il règle sa part
-- ===========================================================================
CREATE OR ALTER TRIGGER trg_ru_EffacerPenalite
ON reservation_utilisateur
AFTER UPDATE
                          AS
BEGIN
    SET NOCOUNT ON;

    IF UPDATE(statut_paiement)
BEGIN
UPDATE u
SET u.penalite_montant = 0
    FROM utilisateur u
        INNER JOIN INSERTED i
ON u.matricule = i.utilisateur_matricule
    INNER JOIN DELETED d
    ON d.reservationid = i.reservationid
    AND d.utilisateur_matricule = i.utilisateur_matricule
WHERE i.statut_paiement = 'PAYE'
  AND d.statut_paiement <> 'PAYE'
  AND u.penalite_montant > 0;
END
END;
GO

-- ===========================================================================
-- TRIGGER 6 : Bloquer les créneaux qui se chevauchent sur un même terrain
-- ===========================================================================
CREATE OR ALTER TRIGGER trg_reservation_SansDoublon
ON reservation
AFTER INSERT, UPDATE
                                  AS
BEGIN
    SET NOCOUNT ON;

    IF EXISTS (
        SELECT 1
        FROM reservation r
        INNER JOIN INSERTED i
               ON r.terrainid = i.terrainid
              AND r.date_reservation = i.date_reservation
              AND r.idreservation <> i.idreservation
              AND r.statut <> 'CANCELLED'
        WHERE r.heure_debut < i.heure_fin
          AND r.heure_fin > i.heure_debut
    )
BEGIN
        RAISERROR('Ce terrain est déjà réservé sur ce créneau.', 16, 1);
ROLLBACK TRANSACTION;
RETURN;
END
END;
GO