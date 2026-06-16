-- Créer les logins SQL Server (niveau serveur, non inclus dans le backup)
IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = 'Utilisateur_Padel')
    CREATE LOGIN Utilisateur_Padel WITH PASSWORD = 'Utilisateur@Padel1';
GO

IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = 'Admin_Padel')
    CREATE LOGIN Admin_Padel WITH PASSWORD = 'Administrateur@Padel1';
GO

-- Supprimer la DB si elle existe déjà (pour repartir proprement depuis le backup)
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'PadelDB')
BEGIN
    ALTER DATABASE PadelDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE PadelDB;
END
GO

-- Restaurer la base depuis le backup exporté
RESTORE DATABASE PadelDB
FROM DISK = '/var/opt/mssql/backup/PadelDB.bak'
WITH
    MOVE 'PadelDB'     TO '/var/opt/mssql/data/PadelDB.mdf',
    MOVE 'PadelDB_log' TO '/var/opt/mssql/data/PadelDB_log.ldf',
    REPLACE, RECOVERY;
GO

USE PadelDB;
GO

-- Rattacher les utilisateurs DB aux logins serveur (orphelin après restore)
IF EXISTS (SELECT name FROM sys.database_principals WHERE name = 'Utilisateur_Padel')
    ALTER USER Utilisateur_Padel WITH LOGIN = Utilisateur_Padel;
ELSE
    CREATE USER Utilisateur_Padel FOR LOGIN Utilisateur_Padel;
GO

IF EXISTS (SELECT name FROM sys.database_principals WHERE name = 'Admin_Padel')
    ALTER USER Admin_Padel WITH LOGIN = Admin_Padel;
ELSE
    CREATE USER Admin_Padel FOR LOGIN Admin_Padel;
GO

-- ── Droits : MOINDRE PRIVILÈGE ───────────────────────────────────────────────
-- Admin : compte privilégié (maintenance complète de la base)
IF IS_ROLEMEMBER('db_owner', 'Admin_Padel') = 0
    EXEC sp_addrolemember 'db_owner', 'Admin_Padel';
GO

-- Utilisateur : lecture + écriture de DONNÉES uniquement (aucun DDL : pas de
-- CREATE/ALTER/DROP, pas de gestion de la base). On retire d'abord db_owner au
-- cas où il serait présent dans le backup, puis on n'accorde que datareader/writer.
IF IS_ROLEMEMBER('db_owner', 'Utilisateur_Padel') = 1
    EXEC sp_droprolemember 'db_owner', 'Utilisateur_Padel';
GO
IF IS_ROLEMEMBER('db_datareader', 'Utilisateur_Padel') = 0
    EXEC sp_addrolemember 'db_datareader', 'Utilisateur_Padel';
GO
IF IS_ROLEMEMBER('db_datawriter', 'Utilisateur_Padel') = 0
    EXEC sp_addrolemember 'db_datawriter', 'Utilisateur_Padel';
GO

PRINT 'Restauration PadelDB terminée avec succès.';
GO
