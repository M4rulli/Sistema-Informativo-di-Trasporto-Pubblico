-- =====================================================
-- 0) SCHEMA (MySQL 8+, InnoDB, utf8mb4)
-- =====================================================
CREATE SCHEMA IF NOT EXISTS `trasporto_pubblico`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;
USE `trasporto_pubblico`;

-- =====================================================
-- 1) TABELLE
-- =====================================================

-- ====================== ENTITÀ =======================

-- Fermata
CREATE TABLE IF NOT EXISTS Fermata (
                                       cod_fermata   VARCHAR(5)  NOT NULL,
                                       lat           DECIMAL(9,6) NOT NULL,
                                       lon           DECIMAL(9,6) NOT NULL,
                                       PRIMARY KEY (cod_fermata)
) ENGINE=InnoDB;

-- Tratta (PK composta: numero_tratta + direzione)
CREATE TABLE IF NOT EXISTS Tratta (
                                      numero_tratta INT      NOT NULL,
                                      direzione     CHAR(1)  NOT NULL,     -- es. 'A' / 'R'
                                      num_veicoli   INT      NOT NULL,
                                      PRIMARY KEY (numero_tratta, direzione)
) ENGINE=InnoDB;

-- Veicolo
CREATE TABLE IF NOT EXISTS Veicolo (
                                       matricola     VARCHAR(4) NOT NULL,
                                       data_acquisto DATE       NOT NULL,
                                       PRIMARY KEY (matricola)
) ENGINE=InnoDB;

-- Conducente
CREATE TABLE IF NOT EXISTS Conducente (
                                          cf               VARCHAR(16) NOT NULL,
                                          nome             VARCHAR(60) NOT NULL,
                                          cognome          VARCHAR(60) NOT NULL,
                                          data_nascita     DATE        NOT NULL,
                                          luogo_nascita    VARCHAR(80) NOT NULL,
                                          numero_patente   VARCHAR(32) NOT NULL,
                                          scadenza_patente DATE        NOT NULL,
                                          PRIMARY KEY (cf)
) ENGINE=InnoDB;

-- Titolo (biglietto/abbonamento) – senza 'utilizzato'
CREATE TABLE IF NOT EXISTS Titolo (
                                      cod_titolo      VARCHAR(12) NOT NULL,
                                      tipo            CHAR(1)     NOT NULL,   -- 'B' / 'A'
                                      scadenza        DATE        NULL,
                                      ultimo_utilizzo DATE        NULL,
                                      PRIMARY KEY (cod_titolo)
) ENGINE=InnoDB;

-- Utenti
CREATE TABLE IF NOT EXISTS Utenti (
                                      username       VARCHAR(64)  NOT NULL,
                                      password_hash  VARCHAR(255) NOT NULL,
                                      ruolo          ENUM('CONDUCENTE','GESTORE') NOT NULL,
                                      cf_conducente  VARCHAR(16)  NULL,
                                      PRIMARY KEY (username),

                                      CONSTRAINT fk_utenti_conducente
                                          FOREIGN KEY (cf_conducente)
                                              REFERENCES Conducente(cf)
                                              ON DELETE RESTRICT
                                              ON UPDATE CASCADE,

    -- 1:1: un CF può comparire al massimo una volta in Utenti
                                      CONSTRAINT uq_utenti_cf_conducente
                                          UNIQUE (cf_conducente)
) ENGINE=InnoDB;

-- =================== ASSOCIAZIONI ====================

-- Comprende: sequenza fermate della tratta
CREATE TABLE IF NOT EXISTS Comprende (
                                         numero_tratta INT        NOT NULL,
                                         direzione     CHAR(1)    NOT NULL,
                                         cod_fermata   VARCHAR(5) NOT NULL,
                                         ordine        INT        NOT NULL,
                                         PRIMARY KEY (numero_tratta, direzione, cod_fermata),
                                         CONSTRAINT fk_comprende_tratta
                                             FOREIGN KEY (numero_tratta, direzione)
                                                 REFERENCES Tratta (numero_tratta, direzione)
                                                 ON DELETE RESTRICT ON UPDATE CASCADE,
                                         CONSTRAINT fk_comprende_fermata
                                             FOREIGN KEY (cod_fermata)
                                                 REFERENCES Fermata (cod_fermata)
                                                 ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Copre: assegnazione veicolo alla tratta
CREATE TABLE IF NOT EXISTS Copre (
                                     numero_tratta INT        NOT NULL,
                                     direzione     CHAR(1)    NOT NULL,
                                     matricola     VARCHAR(4) NOT NULL,
                                     PRIMARY KEY (numero_tratta, direzione, matricola),
                                     CONSTRAINT fk_copre_tratta
                                         FOREIGN KEY (numero_tratta, direzione)
                                             REFERENCES Tratta (numero_tratta, direzione)
                                             ON DELETE RESTRICT ON UPDATE CASCADE,
                                     CONSTRAINT fk_copre_veicolo
                                         FOREIGN KEY (matricola)
                                             REFERENCES Veicolo (matricola)
                                             ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Guida: relazione M:N Conducente–Veicolo
CREATE TABLE IF NOT EXISTS Guida (
                                     cf        VARCHAR(16) NOT NULL,
                                     matricola VARCHAR(4)  NOT NULL,
                                     PRIMARY KEY (cf, matricola),
                                     CONSTRAINT fk_guida_cf
                                         FOREIGN KEY (cf)
                                             REFERENCES Conducente (cf)
                                             ON DELETE RESTRICT ON UPDATE CASCADE,
                                     CONSTRAINT fk_guida_matricola
                                         FOREIGN KEY (matricola)
                                             REFERENCES Veicolo (matricola)
                                             ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Orario_Partenza: orari “prestabiliti” per (tratta,direzione)
CREATE TABLE IF NOT EXISTS Orario_Partenza (
                                               numero_tratta INT     NOT NULL,
                                               direzione     CHAR(1) NOT NULL,
                                               ora_partenza  TIME    NOT NULL,
                                               PRIMARY KEY (numero_tratta, direzione, ora_partenza),
                                               CONSTRAINT fk_orario_tratta
                                                   FOREIGN KEY (numero_tratta, direzione)
                                                       REFERENCES Tratta (numero_tratta, direzione)
                                                       ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- Fa: posizione corrente del veicolo (una riga per veicolo)
CREATE TABLE IF NOT EXISTS Fa (
                                  matricola   VARCHAR(4)  NOT NULL,
                                  cod_fermata VARCHAR(5)  NOT NULL,
                                  PRIMARY KEY (matricola),
                                  CONSTRAINT fk_fa_veicolo
                                      FOREIGN KEY (matricola)
                                          REFERENCES Veicolo (matricola)
                                          ON DELETE CASCADE ON UPDATE CASCADE,
                                  CONSTRAINT fk_fa_fermata
                                      FOREIGN KEY (cod_fermata)
                                          REFERENCES Fermata (cod_fermata)
                                          ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- 2) INDICI e CONSTRAINTS aggiuntivi
-- =====================================================

-- Unicità patente (vincolo di dominio: ogni patente un solo conducente)
ALTER TABLE Conducente
    ADD CONSTRAINT uq_conducente_numero_patente
        UNIQUE (numero_patente);

-- Unicità dell'ordine delle fermate per ciascuna tratta/direzione
ALTER TABLE Comprende
    ADD CONSTRAINT uq_comprende_ordine
        UNIQUE (numero_tratta, direzione, ordine);

-- Ricerca veloce veicoli per fermata (OP01, OP14)
CREATE INDEX idx_fa_cod_fermata
    ON Fa(cod_fermata);

-- Ricerca veloce tratta/direzione partendo dalla matricola del veicolo
-- (OP01: distanza in fermate, OP04/OP05: assegnazioni veicoli/conducenti)
CREATE INDEX idx_copre_matricola
    ON Copre(matricola);


-- =====================================================
-- 2b) UTENTE OWNER DELLE ROUTINE (evita DEFINER=root)
-- =====================================================
DROP USER IF EXISTS 'sp_owner'@'localhost';
CREATE USER 'sp_owner'@'localhost' IDENTIFIED BY 'sp_owner_pwd';

GRANT SELECT, INSERT, UPDATE, DELETE ON `trasporto_pubblico`.* TO 'sp_owner'@'localhost';

GRANT EXECUTE ON `trasporto_pubblico`.* TO 'sp_owner'@'localhost';
GRANT EVENT ON `trasporto_pubblico`.* TO 'sp_owner'@'localhost';

FLUSH PRIVILEGES;

-- =====================================================
-- 3) STORED PROCEDURES
-- =====================================================

-- OP01

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_op01_distanza_fermate $$
CREATE DEFINER=`sp_owner`@`localhost` PROCEDURE sp_op01_distanza_fermate(
    IN p_matricola VARCHAR(4),
    IN p_cod_fermata_target VARCHAR(5)
)
SQL SECURITY DEFINER
BEGIN
    DECLARE v_distanza INT;
    DECLARE v_not_found BOOLEAN DEFAULT FALSE;

    -- Se la SELECT ... INTO non trova righe, scatta NOT FOUND
    DECLARE CONTINUE HANDLER FOR NOT FOUND
        SET v_not_found = TRUE;

    IF p_matricola IS NULL OR p_cod_fermata_target IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20101,
                MESSAGE_TEXT = 'OP01: parametri mancanti';
    END IF;

    -- 1 sola query sulle tabelle: calcolo la distanza
    SELECT (ct.ordine - cc.ordine)
    INTO v_distanza
    FROM Copre cp
             JOIN Fa f
                  ON f.matricola = cp.matricola
             JOIN Comprende cc
                  ON cc.numero_tratta = cp.numero_tratta
                      AND cc.direzione     = cp.direzione
                      AND cc.cod_fermata   = f.cod_fermata
             JOIN Comprende ct
                  ON ct.numero_tratta = cp.numero_tratta
                      AND ct.direzione     = cp.direzione
                      AND ct.cod_fermata   = p_cod_fermata_target
    WHERE cp.matricola = p_matricola
    LIMIT 1;

    IF v_not_found THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20102,
                MESSAGE_TEXT = 'OP01: nessun dato trovato per i parametri forniti';
    END IF;

    SELECT v_distanza AS distanza_fermate;
END $$

DELIMITER ;

-- L1

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_login $$
CREATE DEFINER=`sp_owner`@`localhost` PROCEDURE sp_login(
  IN  p_username VARCHAR(64),
  IN  p_password VARCHAR(255),
  OUT p_ruolo    VARCHAR(16),
  OUT p_cf_conducente VARCHAR(16)
)
SQL SECURITY DEFINER
login_block: BEGIN
  DECLARE v_ruolo VARCHAR(16);
  DECLARE v_cf    VARCHAR(16);
  DECLARE v_not_found BOOL DEFAULT FALSE;

  -- Se non trova righe, NON deve lanciare errore: login fallito pulito
  DECLARE CONTINUE HANDLER FOR NOT FOUND
    SET v_not_found = TRUE;

  -- default: login fallito
  SET p_ruolo = NULL;
  SET p_cf_conducente = NULL;

  IF p_username IS NULL OR p_password IS NULL THEN
    LEAVE login_block;
  END IF;

  SET v_not_found = FALSE;
  SELECT ruolo, cf_conducente
    INTO v_ruolo, v_cf
  FROM Utenti
  WHERE username = p_username
    AND password_hash = SHA2(p_password, 256)
  LIMIT 1;

  -- se non trovato: lascio gli OUT a NULL
  IF v_not_found THEN
    LEAVE login_block;
  END IF;

  SET p_ruolo = v_ruolo;
  SET p_cf_conducente = v_cf;
END $$

DELIMITER ;

-- OP02

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_op02_prossima_partenza $$
CREATE DEFINER=`sp_owner`@`localhost` PROCEDURE sp_op02_prossima_partenza(
    IN p_numero_tratta INT,
    IN p_direzione CHAR(1)
)
SQL SECURITY DEFINER
BEGIN
    DECLARE v_next TIME;

    IF p_numero_tratta IS NULL OR p_direzione IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20201,
                MESSAGE_TEXT = 'OP02: parametri mancanti';
    END IF;

    SELECT MIN(ora_partenza) INTO v_next
    FROM Orario_Partenza
    WHERE numero_tratta = p_numero_tratta
      AND direzione     = p_direzione
      AND ora_partenza >= CURTIME();

    IF v_next IS NULL THEN
        SELECT MIN(ora_partenza) INTO v_next
        FROM Orario_Partenza
        WHERE numero_tratta = p_numero_tratta
          AND direzione     = p_direzione;
    END IF;

    IF v_next IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20202,
                MESSAGE_TEXT = 'OP02: nessun orario configurato per la tratta';
    END IF;

    SELECT v_next AS prossima_partenza;
END $$

DELIMITER ;

-- OP03a

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_op03a_validazione_biglietto $$
CREATE DEFINER=`sp_owner`@`localhost` PROCEDURE sp_op03a_validazione_biglietto(
    IN p_cod_titolo VARCHAR(12)
)
SQL SECURITY DEFINER
BEGIN
    DECLARE v_tipo CHAR(1);
    DECLARE v_scad DATE;
    DECLARE v_last DATE;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;

    IF p_cod_titolo IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20301,
                MESSAGE_TEXT = 'OP03a: codice titolo mancante';
    END IF;

    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    START TRANSACTION;

    SELECT tipo, scadenza, ultimo_utilizzo
    INTO v_tipo, v_scad, v_last
    FROM Titolo
    WHERE cod_titolo = p_cod_titolo
        FOR UPDATE;

    IF v_tipo IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20302,
                MESSAGE_TEXT = 'OP03a: titolo inesistente';
    END IF;

    IF v_tipo <> 'B' THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20303,
                MESSAGE_TEXT = 'OP03a: titolo non è un biglietto';
    END IF;

    IF v_last IS NOT NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20304,
                MESSAGE_TEXT = 'OP03a: biglietto già utilizzato';
    END IF;

    IF v_scad IS NOT NULL AND v_scad < CURDATE() THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20305,
                MESSAGE_TEXT = 'OP03a: biglietto scaduto';
    END IF;

    UPDATE Titolo
    SET ultimo_utilizzo = CURDATE()
    WHERE cod_titolo = p_cod_titolo;

    COMMIT;

    SELECT 'OK' AS esito, p_cod_titolo AS cod_titolo, CURDATE() AS data_validazione;
END $$

DELIMITER ;

-- OP03b

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_op03b_validazione_abbonamento $$
CREATE DEFINER=`sp_owner`@`localhost` PROCEDURE sp_op03b_validazione_abbonamento(
    IN p_cod_titolo VARCHAR(12)
)
SQL SECURITY DEFINER
BEGIN
    DECLARE v_tipo CHAR(1);
    DECLARE v_scad DATE;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;

    IF p_cod_titolo IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20401,
                MESSAGE_TEXT = 'OP03b: codice titolo mancante';
    END IF;

    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    START TRANSACTION;

    SELECT tipo, scadenza
    INTO v_tipo, v_scad
    FROM Titolo
    WHERE cod_titolo = p_cod_titolo
        FOR UPDATE;

    IF v_tipo IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20402,
                MESSAGE_TEXT = 'OP03b: titolo inesistente';
    END IF;

    IF v_tipo <> 'A' THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20403,
                MESSAGE_TEXT = 'OP03b: titolo non è un abbonamento';
    END IF;

    IF v_scad IS NULL OR v_scad < CURDATE() THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20404,
                MESSAGE_TEXT = 'OP03b: abbonamento scaduto o senza scadenza';
    END IF;

    UPDATE Titolo
    SET ultimo_utilizzo = CURDATE()
    WHERE cod_titolo = p_cod_titolo;

    COMMIT;

    SELECT 'OK' AS esito, p_cod_titolo AS cod_titolo, v_scad AS scadenza;
END $$

DELIMITER ;

-- OP04

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_op04_assegna_veicolo_tratta $$
CREATE DEFINER=`sp_owner`@`localhost` PROCEDURE sp_op04_assegna_veicolo_tratta(
    IN p_matricola VARCHAR(4),
    IN p_numero_tratta INT,
    IN p_direzione CHAR(1)
)
SQL SECURITY DEFINER
BEGIN
    -- Violazione vincoli di unicità/PK (qui usato per segnalare RV2/duplicati)
    DECLARE EXIT HANDLER FOR 1062
        BEGIN
            SIGNAL SQLSTATE '45000'
                SET MYSQL_ERRNO = 20502,
                    MESSAGE_TEXT = 'OP04/RV2: veicolo già assegnato a una tratta (o assegnazione già esistente)';
        END;

    IF p_matricola IS NULL OR p_numero_tratta IS NULL OR p_direzione IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20501,
                MESSAGE_TEXT = 'OP04: parametri mancanti';
    END IF;

    -- Single statement: inserisce solo se esistono veicolo e tratta
    INSERT INTO Copre(numero_tratta, direzione, matricola)
    SELECT p_numero_tratta, p_direzione, p_matricola
    FROM Veicolo v
             JOIN Tratta t
                  ON t.numero_tratta = p_numero_tratta
                      AND t.direzione     = p_direzione
    WHERE v.matricola = p_matricola;

    IF ROW_COUNT() = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20503,
                MESSAGE_TEXT = 'OP04: veicolo o tratta inesistenti';
    END IF;

    SELECT 'OK' AS esito,
           p_matricola AS matricola,
           p_numero_tratta AS numero_tratta,
           p_direzione AS direzione;
END $$

DELIMITER ;

-- OP05

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_op05_assegna_conducente_veicolo $$
CREATE DEFINER=`sp_owner`@`localhost` PROCEDURE sp_op05_assegna_conducente_veicolo(
    IN p_cf VARCHAR(16),
    IN p_matricola VARCHAR(4)
)
SQL SECURITY DEFINER
BEGIN
    -- Duplicate key: la coppia (cf, matricola) è già presente (PK di Guida)
    DECLARE EXIT HANDLER FOR 1062
        BEGIN
            SIGNAL SQLSTATE '45000'
                SET MYSQL_ERRNO = 20602,
                    MESSAGE_TEXT = 'OP05: associazione conducente-veicolo già esistente';
        END;

    IF p_cf IS NULL OR p_matricola IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20601,
                MESSAGE_TEXT = 'OP05: parametri mancanti';
    END IF;

    -- Single statement: inserisce solo se esistono conducente e veicolo
    INSERT INTO Guida(cf, matricola)
    SELECT c.cf, v.matricola
    FROM Conducente c
             JOIN Veicolo v
                  ON v.matricola = p_matricola
    WHERE c.cf = p_cf;

    IF ROW_COUNT() = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20603,
                MESSAGE_TEXT = 'OP05: conducente o veicolo inesistenti';
    END IF;

    SELECT 'OK' AS esito,
           p_cf AS cf,
           p_matricola AS matricola;
END $$

DELIMITER ;

-- OP06

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_op06a_inserisci_conducente $$
CREATE DEFINER=`sp_owner`@`localhost` PROCEDURE sp_op06a_inserisci_conducente(
    IN p_cf VARCHAR(16),
    IN p_nome VARCHAR(60),
    IN p_cognome VARCHAR(60),
    IN p_data_nascita DATE,
    IN p_luogo_nascita VARCHAR(80),
    IN p_numero_patente VARCHAR(32),
    IN p_scadenza_patente DATE
)
SQL SECURITY DEFINER
BEGIN
    -- Duplicate key: CF già presente (PK) o numero_patente già presente (UNIQUE)
    DECLARE EXIT HANDLER FOR 1062
        BEGIN
            SIGNAL SQLSTATE '45000'
                SET MYSQL_ERRNO = 20612,
                    MESSAGE_TEXT = 'OP06a: conducente già esistente o patente già registrata';
        END;

    IF p_cf IS NULL OR p_nome IS NULL OR p_cognome IS NULL OR p_data_nascita IS NULL
        OR p_luogo_nascita IS NULL OR p_numero_patente IS NULL OR p_scadenza_patente IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20611,
                MESSAGE_TEXT = 'OP06a: parametri mancanti';
    END IF;

    INSERT INTO Conducente(
        cf, nome, cognome, data_nascita, luogo_nascita, numero_patente, scadenza_patente
    )
    VALUES(
              p_cf, p_nome, p_cognome, p_data_nascita, p_luogo_nascita, p_numero_patente, p_scadenza_patente
          );

    SELECT 'OK' AS esito, p_cf AS cf;
END $$

DELIMITER ;

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_op06b_aggiorna_conducente $$
CREATE DEFINER=`sp_owner`@`localhost` PROCEDURE sp_op06b_aggiorna_conducente(
    IN p_cf VARCHAR(16),
    IN p_nome VARCHAR(60),
    IN p_cognome VARCHAR(60),
    IN p_data_nascita DATE,
    IN p_luogo_nascita VARCHAR(80),
    IN p_numero_patente VARCHAR(32),
    IN p_scadenza_patente DATE
)
SQL SECURITY DEFINER
BEGIN
    DECLARE EXIT HANDLER FOR 1062
        BEGIN
            SIGNAL SQLSTATE '45000'
                SET MYSQL_ERRNO = 20622,
                    MESSAGE_TEXT = 'OP06b: patente già registrata su un altro conducente';
        END;

    IF p_cf IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20621,
                MESSAGE_TEXT = 'OP06b: CF mancante';
    END IF;

    UPDATE Conducente
    SET nome = COALESCE(p_nome, nome),
        cognome = COALESCE(p_cognome, cognome),
        data_nascita = COALESCE(p_data_nascita, data_nascita),
        luogo_nascita = COALESCE(p_luogo_nascita, luogo_nascita),
        numero_patente = COALESCE(p_numero_patente, numero_patente),
        scadenza_patente = COALESCE(p_scadenza_patente, scadenza_patente)
    WHERE cf = p_cf;

    IF ROW_COUNT() = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20623,
                MESSAGE_TEXT = 'OP06b: conducente inesistente';
    END IF;

    SELECT 'OK' AS esito, p_cf AS cf;
END $$

DELIMITER ;


-- OP07

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_op07_orario_crud $$
CREATE DEFINER=`sp_owner`@`localhost` PROCEDURE sp_op07_orario_crud(
  IN p_azione CHAR(3),           -- 'INS' | 'DEL' | 'UPD'
  IN p_numero_tratta INT,
  IN p_direzione CHAR(1),
  IN p_ora TIME,                 -- usata in INS/DEL come ora interessata
  IN p_ora_new TIME              -- usata solo in UPD (nuovo orario)
)
SQL SECURITY DEFINER
BEGIN
  -- Duplicati sulla PK (tratta, direzione, ora)
  DECLARE EXIT HANDLER FOR 1062
  BEGIN
    SIGNAL SQLSTATE '45000'
      SET MYSQL_ERRNO = 20740,
          MESSAGE_TEXT = 'OP07: conflitto, orario già presente';
  END;

  IF p_azione IS NULL OR p_numero_tratta IS NULL OR p_direzione IS NULL THEN
    SIGNAL SQLSTATE '45000'
      SET MYSQL_ERRNO = 20741,
          MESSAGE_TEXT = 'OP07: parametri mancanti';
  END IF;

  IF p_azione = 'INS' THEN
    IF p_ora IS NULL THEN
      SIGNAL SQLSTATE '45000'
        SET MYSQL_ERRNO = 20743,
            MESSAGE_TEXT = 'OP07/INS: ora mancante';
    END IF;

    INSERT INTO Orario_Partenza(numero_tratta, direzione, ora_partenza)
    VALUES (p_numero_tratta, p_direzione, p_ora);

    SELECT 'OK' AS esito, 'INS' AS azione;

  ELSEIF p_azione = 'DEL' THEN
    IF p_ora IS NULL THEN
      SIGNAL SQLSTATE '45000'
        SET MYSQL_ERRNO = 20744,
            MESSAGE_TEXT = 'OP07/DEL: ora mancante';
    END IF;

    DELETE FROM Orario_Partenza
    WHERE numero_tratta = p_numero_tratta
      AND direzione     = p_direzione
      AND ora_partenza  = p_ora;

    IF ROW_COUNT() = 0 THEN
      SIGNAL SQLSTATE '45000'
        SET MYSQL_ERRNO = 20745,
            MESSAGE_TEXT = 'OP07/DEL: orario inesistente';
    END IF;

    SELECT 'OK' AS esito, 'DEL' AS azione;

  ELSEIF p_azione = 'UPD' THEN
    IF p_ora IS NULL OR p_ora_new IS NULL THEN
      SIGNAL SQLSTATE '45000'
        SET MYSQL_ERRNO = 20746,
            MESSAGE_TEXT = 'OP07/UPD: ora o ora_new mancanti';
    END IF;

    UPDATE Orario_Partenza
    SET ora_partenza = p_ora_new
    WHERE numero_tratta = p_numero_tratta
      AND direzione     = p_direzione
      AND ora_partenza  = p_ora;

    IF ROW_COUNT() = 0 THEN
      SIGNAL SQLSTATE '45000'
        SET MYSQL_ERRNO = 20747,
            MESSAGE_TEXT = 'OP07/UPD: orario inesistente';
    END IF;

    SELECT 'OK' AS esito, 'UPD' AS azione;

  ELSE
    SIGNAL SQLSTATE '45000'
      SET MYSQL_ERRNO = 20748,
          MESSAGE_TEXT = 'OP07: azione non valida (usa INS/DEL/UPD)';
  END IF;
END $$

DELIMITER ;

-- OP08a

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_op08a_emetti_biglietti_lotto $$
CREATE DEFINER=`sp_owner`@`localhost` PROCEDURE sp_op08a_emetti_biglietti_lotto(
    IN p_quantita INT
)
SQL SECURITY DEFINER
BEGIN
            DECLARE v_i INT DEFAULT 0;
            DECLARE v_cod VARCHAR(12);
            DECLARE v_prefix VARCHAR(6);  -- prefisso lotto (YYMMDD)

            DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    ROLLBACK;
                    RESIGNAL;
                END;

            IF p_quantita IS NULL OR p_quantita <= 0 THEN
                SIGNAL SQLSTATE '45000'
                    SET MYSQL_ERRNO = 20801,
                        MESSAGE_TEXT = 'OP08a: quantità non valida';
            END IF;

            -- soglia di sicurezza per evitare lotti enormi per errore
            IF p_quantita > 50000 THEN
                SIGNAL SQLSTATE '45000'
                    SET MYSQL_ERRNO = 20802,
                        MESSAGE_TEXT = 'OP08a: quantità troppo elevata';
            END IF;

            -- Prefisso lotto: YYMMDD (es. 240601)
            SET v_prefix = DATE_FORMAT(NOW(), '%y%m%d');  -- 6 char

            SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
            START TRANSACTION;

            WHILE v_i < p_quantita DO
                    -- Codice 12 char: 'B' + 6-char prefix + 5-digit counter (00000-99999)
                    SET v_cod = CONCAT('B', v_prefix, LPAD(v_i, 5, '0'));

                    INSERT INTO Titolo(cod_titolo, tipo, scadenza, ultimo_utilizzo)
                    VALUES (v_cod, 'B', NULL, NULL);

                    SET v_i = v_i + 1;
                END WHILE;

            COMMIT;

            SELECT 'OK' AS esito,
                   p_quantita AS biglietti_emessi,
                   v_prefix AS lotto_prefix;
        END $$

DELIMITER ;


-- OP08b

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_op08b_emetti_abbonamento $$
CREATE DEFINER=`sp_owner`@`localhost` PROCEDURE sp_op08b_emetti_abbonamento(
    IN p_cod_titolo VARCHAR(12),
    IN p_scadenza DATE
)
SQL SECURITY DEFINER
BEGIN
    DECLARE EXIT HANDLER FOR 1062
        BEGIN
            SIGNAL SQLSTATE '45000'
                SET MYSQL_ERRNO = 20812,
                    MESSAGE_TEXT = 'OP08b: codice titolo già esistente';
        END;

    IF p_cod_titolo IS NULL OR p_scadenza IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20811,
                MESSAGE_TEXT = 'OP08b: parametri mancanti';
    END IF;

    IF p_scadenza < CURDATE() THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20813,
                MESSAGE_TEXT = 'OP08b: scadenza non valida (nel passato)';
    END IF;

    INSERT INTO Titolo(cod_titolo, tipo, scadenza, ultimo_utilizzo)
    VALUES (p_cod_titolo, 'A', p_scadenza, NULL);

    SELECT 'OK' AS esito, p_cod_titolo AS cod_titolo, p_scadenza AS scadenza;
END $$

DELIMITER ;

-- OP09

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_op09_veicolo $$
CREATE DEFINER=`sp_owner`@`localhost` PROCEDURE sp_op09_veicolo(
    IN p_azione CHAR(3),          -- 'INS' | 'UPD'
    IN p_matricola VARCHAR(4),
    IN p_data_acquisto DATE
)
SQL SECURITY DEFINER
BEGIN
    -- Duplicate key su PK(matricola)
    DECLARE EXIT HANDLER FOR 1062
        BEGIN
            SIGNAL SQLSTATE '45000'
                SET MYSQL_ERRNO = 20902,
                    MESSAGE_TEXT = 'OP09: matricola già esistente';
        END;

    IF p_azione IS NULL OR p_matricola IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20901,
                MESSAGE_TEXT = 'OP09: azione o matricola mancanti';
    END IF;

    IF p_azione = 'INS' THEN
        IF p_data_acquisto IS NULL THEN
            SIGNAL SQLSTATE '45000'
                SET MYSQL_ERRNO = 20903,
                    MESSAGE_TEXT = 'OP09/INS: data_acquisto mancante';
        END IF;

        INSERT INTO Veicolo(matricola, data_acquisto)
        VALUES (p_matricola, p_data_acquisto);

        SELECT 'OK' AS esito, 'INS' AS azione, p_matricola AS matricola;

    ELSEIF p_azione = 'UPD' THEN
        -- Aggiornamento puntuale (solo campi forniti)
        UPDATE Veicolo
        SET data_acquisto = COALESCE(p_data_acquisto, data_acquisto)
        WHERE matricola = p_matricola;

        IF ROW_COUNT() = 0 THEN
            SIGNAL SQLSTATE '45000'
                SET MYSQL_ERRNO = 20904,
                    MESSAGE_TEXT = 'OP09/UPD: veicolo inesistente';
        END IF;

        SELECT 'OK' AS esito, 'UPD' AS azione, p_matricola AS matricola;

    ELSE
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 20905,
                MESSAGE_TEXT = 'OP09: azione non valida (usa INS/UPD)';
    END IF;
END $$

DELIMITER ;

-- OP10

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_op10_fermata $$
CREATE DEFINER=`sp_owner`@`localhost` PROCEDURE sp_op10_fermata(
    IN p_azione CHAR(3),            -- 'INS' | 'UPD'
    IN p_cod_fermata VARCHAR(5),
    IN p_lat DECIMAL(9,6),
    IN p_lon DECIMAL(9,6)
)
SQL SECURITY DEFINER
BEGIN
    -- Duplicate key su PK(cod_fermata)
    DECLARE EXIT HANDLER FOR 1062
        BEGIN
            SIGNAL SQLSTATE '45000'
                SET MYSQL_ERRNO = 21002,
                    MESSAGE_TEXT = 'OP10: fermata già esistente';
        END;

    IF p_azione IS NULL OR p_cod_fermata IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 21001,
                MESSAGE_TEXT = 'OP10: azione o codice fermata mancanti';
    END IF;

    IF p_azione = 'INS' THEN
        IF p_lat IS NULL OR p_lon IS NULL THEN
            SIGNAL SQLSTATE '45000'
                SET MYSQL_ERRNO = 21003,
                    MESSAGE_TEXT = 'OP10/INS: coordinate mancanti';
        END IF;

        INSERT INTO Fermata(cod_fermata, lat, lon)
        VALUES (p_cod_fermata, p_lat, p_lon);

        SELECT 'OK' AS esito, 'INS' AS azione, p_cod_fermata AS cod_fermata;

    ELSEIF p_azione = 'UPD' THEN
        UPDATE Fermata
        SET lat = COALESCE(p_lat, lat),
            lon = COALESCE(p_lon, lon)
        WHERE cod_fermata = p_cod_fermata;

        IF ROW_COUNT() = 0 THEN
            SIGNAL SQLSTATE '45000'
                SET MYSQL_ERRNO = 21004,
                    MESSAGE_TEXT = 'OP10/UPD: fermata inesistente';
        END IF;

        SELECT 'OK' AS esito, 'UPD' AS azione, p_cod_fermata AS cod_fermata;

    ELSE
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 21005,
                MESSAGE_TEXT = 'OP10: azione non valida (usa INS/UPD)';
    END IF;
END $$

DELIMITER ;

-- OP11

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_op11_aggiungi_fermata_tratta $$
CREATE DEFINER=`sp_owner`@`localhost` PROCEDURE sp_op11_aggiungi_fermata_tratta(
    IN p_numero_tratta INT,
    IN p_direzione CHAR(1),
    IN p_cod_fermata VARCHAR(5)
)
SQL SECURITY DEFINER
BEGIN
    DECLARE v_next_ordine INT;

    -- Se succede qualunque errore dentro la transazione: rollback immediato
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;

    IF p_numero_tratta IS NULL OR p_direzione IS NULL OR p_cod_fermata IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 21101,
                MESSAGE_TEXT = 'OP11: parametri mancanti';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM Tratta
        WHERE numero_tratta = p_numero_tratta AND direzione = p_direzione
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 21102,
                MESSAGE_TEXT = 'OP11: tratta/direzione inesistente';
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM Fermata
        WHERE cod_fermata = p_cod_fermata
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 21103,
                MESSAGE_TEXT = 'OP11: fermata inesistente';
    END IF;

    SET TRANSACTION ISOLATION LEVEL REPEATABLE READ;
    START TRANSACTION;

    /*
      Lock sulla riga Tratta: serializza l'assegnazione anche se Comprende è ancora vuota
    */
    SELECT 1
    FROM Tratta
    WHERE numero_tratta = p_numero_tratta
      AND direzione     = p_direzione
    FOR UPDATE;

    -- Calcolo ordine successivo (1 se la tratta è ancora vuota)
    SELECT COALESCE(MAX(ordine), 0) + 1
    INTO v_next_ordine
    FROM Comprende
    WHERE numero_tratta = p_numero_tratta
      AND direzione     = p_direzione;

    -- Inserimento associazione (può fallire per PK/UNIQUE -> SQLEXCEPTION -> handler -> rollback)
    INSERT INTO Comprende(numero_tratta, direzione, cod_fermata, ordine)
    VALUES (p_numero_tratta, p_direzione, p_cod_fermata, v_next_ordine);

    COMMIT;

    SELECT 'OK' AS esito,
           p_numero_tratta AS numero_tratta,
           p_direzione AS direzione,
           p_cod_fermata AS cod_fermata,
           v_next_ordine AS ordine;
END $$

DELIMITER ;

-- OP12


DELIMITER $$

DROP PROCEDURE IF EXISTS sp_op12_avanza_fermata $$
CREATE DEFINER=`sp_owner`@`localhost` PROCEDURE sp_op12_avanza_fermata(
    IN p_matricola VARCHAR(4)
)
SQL SECURITY DEFINER
BEGIN
    DECLARE v_curr VARCHAR(5);
    DECLARE v_next VARCHAR(5);
    DECLARE v_num_tratta INT;
    DECLARE v_dir CHAR(1);
    DECLARE v_ord_curr INT;

    DECLARE v_not_found BOOL DEFAULT FALSE;

    -- Se una SELECT ... INTO non trova righe
    DECLARE CONTINUE HANDLER FOR NOT FOUND
        SET v_not_found = TRUE;

    -- Se succede qualunque errore: rollback immediato e rilancio
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            ROLLBACK;
            RESIGNAL;
        END;

    IF p_matricola IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 21201,
                MESSAGE_TEXT = 'OP12: matricola mancante';
    END IF;

    SET TRANSACTION ISOLATION LEVEL READ COMMITTED;
    START TRANSACTION;

    /*
      Risorsa critica: riga Fa del veicolo.
      Locking read (FOR UPDATE): il lock resta fino a COMMIT/ROLLBACK e serializza OP12 concorrenti sullo stesso veicolo.
    */
    SET v_not_found = FALSE;
    SELECT cod_fermata
    INTO v_curr
    FROM Fa
    WHERE matricola = p_matricola
        FOR UPDATE;

    IF v_not_found THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 21202,
                MESSAGE_TEXT = 'OP12: posizione veicolo non disponibile';
    END IF;

    -- Tratta/direzione del veicolo
    SET v_not_found = FALSE;
    SELECT numero_tratta, direzione
    INTO v_num_tratta, v_dir
    FROM Copre
    WHERE matricola = p_matricola
    LIMIT 1;

    IF v_not_found THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 21203,
                MESSAGE_TEXT = 'OP12: veicolo non assegnato a tratta';
    END IF;

    -- Ordine della fermata corrente nella sequenza
    SET v_not_found = FALSE;
    SELECT ordine
    INTO v_ord_curr
    FROM Comprende
    WHERE numero_tratta = v_num_tratta
      AND direzione     = v_dir
      AND cod_fermata   = v_curr;

    IF v_not_found THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 21204,
                MESSAGE_TEXT = 'OP12: fermata corrente non presente nella tratta';
    END IF;

    -- Fermata successiva (ordine+1); se non esiste, riparto dalla prima (capolinea)
    SET v_not_found = FALSE;
    SELECT cod_fermata
    INTO v_next
    FROM Comprende
    WHERE numero_tratta = v_num_tratta
      AND direzione     = v_dir
      AND ordine        = v_ord_curr + 1;

    IF v_not_found THEN
        SET v_not_found = FALSE;
        SELECT cod_fermata
        INTO v_next
        FROM Comprende
        WHERE numero_tratta = v_num_tratta
          AND direzione     = v_dir
        ORDER BY ordine
        LIMIT 1;

        IF v_not_found THEN
            SIGNAL SQLSTATE '45000'
                SET MYSQL_ERRNO = 21205,
                    MESSAGE_TEXT = 'OP12: tratta senza fermate (Comprende vuota)';
        END IF;
    END IF;

    -- Aggiornamento posizione
    UPDATE Fa
    SET cod_fermata = v_next
    WHERE matricola = p_matricola;

    COMMIT;

    SELECT 'OK' AS esito,
           p_matricola AS matricola,
           v_num_tratta AS numero_tratta,
           v_dir AS direzione,
           v_curr AS fermata_precedente,
           v_next AS fermata_nuova;
END $$

DELIMITER ;

-- OP13

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_op13_prossime_fermate $$
CREATE DEFINER=`sp_owner`@`localhost` PROCEDURE sp_op13_prossime_fermate(
    IN p_matricola VARCHAR(4),
    IN p_n INT
)
SQL SECURITY DEFINER
BEGIN
    DECLARE v_num_tratta INT;
    DECLARE v_dir CHAR(1);
    DECLARE v_ord_curr INT;

    DECLARE v_not_found BOOL DEFAULT FALSE;
    DECLARE CONTINUE HANDLER FOR NOT FOUND
        SET v_not_found = TRUE;

    IF p_matricola IS NULL OR p_n IS NULL OR p_n <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 21301,
                MESSAGE_TEXT = 'OP13: parametri non validi (matricola o N)';
    END IF;

    -- 1) Recupero tratta/direzione e ordine della fermata corrente
    SET v_not_found = FALSE;
    SELECT cp.numero_tratta, cp.direzione, cc.ordine
    INTO v_num_tratta, v_dir, v_ord_curr
    FROM Copre cp
             JOIN Fa f ON f.matricola = cp.matricola
             JOIN Comprende cc
                  ON cc.numero_tratta = cp.numero_tratta
                      AND cc.direzione     = cp.direzione
                      AND cc.cod_fermata   = f.cod_fermata
    WHERE cp.matricola = p_matricola
    LIMIT 1;

    IF v_not_found THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 21302,
                MESSAGE_TEXT = 'OP13: veicolo non trovato o dati incoerenti';
    END IF;

    -- 2) Prossime N fermate
    SELECT cod_fermata, ordine
    FROM Comprende
    WHERE numero_tratta = v_num_tratta
      AND direzione     = v_dir
      AND ordine        > v_ord_curr
    ORDER BY ordine
    LIMIT p_n;

END $$

DELIMITER ;

-- OP14

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_op14_consulta_orari $$
CREATE DEFINER=`sp_owner`@`localhost` PROCEDURE sp_op14_consulta_orari(
    IN p_numero_tratta INT,
    IN p_direzione CHAR(1)
)
SQL SECURITY DEFINER
BEGIN
    DECLARE v_not_found BOOL DEFAULT FALSE;
    DECLARE v_dummy INT;

    DECLARE CONTINUE HANDLER FOR NOT FOUND
        SET v_not_found = TRUE;

    IF p_numero_tratta IS NULL OR p_direzione IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 21401,
                MESSAGE_TEXT = 'OP14: parametri mancanti';
    END IF;

    -- 1) Verifica che la tratta/direzione esista
    SET v_not_found = FALSE;
    SELECT 1
    INTO v_dummy
    FROM Tratta
    WHERE numero_tratta = p_numero_tratta
      AND direzione     = p_direzione;

    IF v_not_found THEN
        SIGNAL SQLSTATE '45000'
            SET MYSQL_ERRNO = 21402,
                MESSAGE_TEXT = 'OP14: tratta/direzione inesistente';
    END IF;

    -- 2) Result set #1: fermate in ordine
    SELECT c.ordine,
           c.cod_fermata,
           f.lat,
           f.lon
    FROM Comprende c
             JOIN Fermata f ON f.cod_fermata = c.cod_fermata
    WHERE c.numero_tratta = p_numero_tratta
      AND c.direzione     = p_direzione
    ORDER BY c.ordine;

    -- 3) Result set #2: orari di partenza (timetable)
    SELECT ora_partenza
    FROM Orario_Partenza
    WHERE numero_tratta = p_numero_tratta
      AND direzione     = p_direzione
    ORDER BY ora_partenza;

END $$

DELIMITER ;

-- =====================================================
-- 4) TRIGGERS
-- =====================================================

DELIMITER $$

DROP TRIGGER IF EXISTS Fa_RV1_before_insert $$
CREATE TRIGGER Fa_RV1_before_insert
    BEFORE INSERT ON Fa
    FOR EACH ROW
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
        BEGIN
            SIGNAL SQLSTATE '45010'
                SET MESSAGE_TEXT = 'La fermata non appartiene alla tratta del veicolo';
        END;

    -- Controllo: la fermata deve appartenere alla tratta del veicolo
    IF NOT EXISTS (
        SELECT 1
        FROM Copre c
                 JOIN Comprende comp
                      ON comp.numero_tratta = c.numero_tratta
                          AND comp.direzione     = c.direzione
                          AND comp.cod_fermata   = NEW.cod_fermata
        WHERE c.matricola = NEW.matricola
    ) THEN
        SIGNAL SQLSTATE '45010'
            SET MESSAGE_TEXT = 'La fermata non appartiene alla tratta del veicolo';
    END IF;
END$$

DELIMITER ;

DELIMITER $$

DROP TRIGGER IF EXISTS Fa_RV1_before_update $$
CREATE TRIGGER Fa_RV1_before_update
    BEFORE UPDATE ON Fa
    FOR EACH ROW
BEGIN
  rv1: BEGIN
    -- Se non cambia la fermata, non serve controllare
    IF NEW.cod_fermata = OLD.cod_fermata THEN
      LEAVE rv1;
    END IF;

    -- Controllo: la nuova fermata deve appartenere alla tratta coperta dal veicolo
    IF NOT EXISTS (
      SELECT 1
      FROM Copre c
        JOIN Comprende comp
          ON comp.numero_tratta = c.numero_tratta
         AND comp.direzione     = c.direzione
         AND comp.cod_fermata   = NEW.cod_fermata
      WHERE c.matricola = NEW.matricola
    ) THEN
      SIGNAL SQLSTATE '45010'
        SET MESSAGE_TEXT = 'RV1: la fermata non appartiene alla tratta del veicolo';
    END IF;
  END rv1;
END $$

DELIMITER ;

DELIMITER $$

DROP TRIGGER IF EXISTS Tratta_chk_dir_bi $$
CREATE TRIGGER Tratta_chk_dir_bi
    BEFORE INSERT ON Tratta
    FOR EACH ROW
BEGIN
    IF NEW.direzione NOT IN ('A','R') THEN
        SIGNAL SQLSTATE '45011'
            SET MESSAGE_TEXT = 'Direzione invalida: deve essere A o R';
    END IF;
END$$

DROP TRIGGER IF EXISTS Tratta_chk_dir_bu $$
CREATE TRIGGER Tratta_chk_dir_bu
    BEFORE UPDATE ON Tratta
    FOR EACH ROW
BEGIN
    IF NEW.direzione NOT IN ('A','R') THEN
        SIGNAL SQLSTATE '45012'
            SET MESSAGE_TEXT = 'Direzione invalida: deve essere A o R';
    END IF;
END$$

DELIMITER ;

DELIMITER $$

DROP TRIGGER IF EXISTS Comprende_chk_ordine_bi $$
CREATE TRIGGER Comprende_chk_ordine_bi
    BEFORE INSERT ON Comprende
    FOR EACH ROW
BEGIN
    IF NEW.ordine < 1 THEN
        SIGNAL SQLSTATE '45042' SET MESSAGE_TEXT = 'Comprende: ordine deve essere >= 1';
    END IF;
END $$

DROP TRIGGER IF EXISTS Comprende_chk_ordine_bu $$
CREATE TRIGGER Comprende_chk_ordine_bu
    BEFORE UPDATE ON Comprende
    FOR EACH ROW
BEGIN
    IF NEW.ordine < 1 THEN
        SIGNAL SQLSTATE '45043' SET MESSAGE_TEXT = 'Comprende: ordine deve essere >= 1 (update)';
    END IF;
END $$

DELIMITER ;

DELIMITER $$

DROP TRIGGER IF EXISTS Fermata_chk_latlon_bi $$
CREATE TRIGGER Fermata_chk_latlon_bi
    BEFORE INSERT ON Fermata
    FOR EACH ROW
BEGIN
    IF NEW.lat < -90 OR NEW.lat > 90 OR NEW.lon < -180 OR NEW.lon > 180 THEN
        SIGNAL SQLSTATE '45040' SET MESSAGE_TEXT = 'Coordinate fuori range';
    END IF;
END $$

DROP TRIGGER IF EXISTS Fermata_chk_latlon_bu $$
CREATE TRIGGER Fermata_chk_latlon_bu
    BEFORE UPDATE ON Fermata
    FOR EACH ROW
BEGIN
    IF NEW.lat < -90 OR NEW.lat > 90 OR NEW.lon < -180 OR NEW.lon > 180 THEN
        SIGNAL SQLSTATE '45041' SET MESSAGE_TEXT = 'Coordinate fuori range (update)';
    END IF;
END $$

DELIMITER ;

DELIMITER $$

DROP TRIGGER IF EXISTS Guida_chk_patente_bi $$
CREATE TRIGGER Guida_chk_patente_bi
    BEFORE INSERT ON Guida
    FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM Conducente
               WHERE cf = NEW.cf AND scadenza_patente < CURDATE()) THEN
        SIGNAL SQLSTATE '45044' SET MESSAGE_TEXT = 'Guida: patente scaduta';
    END IF;
END $$

DROP TRIGGER IF EXISTS Guida_chk_patente_bu $$
CREATE TRIGGER Guida_chk_patente_bu
    BEFORE UPDATE ON Guida
    FOR EACH ROW
BEGIN
    IF NEW.cf <> OLD.cf AND EXISTS (SELECT 1 FROM Conducente
                                    WHERE cf = NEW.cf AND scadenza_patente < CURDATE()) THEN
        SIGNAL SQLSTATE '45045' SET MESSAGE_TEXT = 'Guida: patente scaduta (update)';
    END IF;
END $$

DELIMITER ;

-- =====================================================
-- 5) UTENTI E PRIVILEGI
-- =====================================================

-- === LOGIN: può solo autenticare ===
DROP USER IF EXISTS 'login'@'localhost';
CREATE USER 'login'@'localhost' IDENTIFIED BY 'login_pwd';

GRANT EXECUTE ON PROCEDURE trasporto_pubblico.sp_login
    TO 'login'@'localhost';


-- === UTENTE PUBBLICO: sola consultazione ===
DROP USER IF EXISTS 'utente'@'localhost';
CREATE USER 'utente'@'localhost' IDENTIFIED BY 'utente_pwd';

-- OP01 – Distanza veicolo in fermate
GRANT EXECUTE ON PROCEDURE trasporto_pubblico.sp_op01_distanza_fermate
    TO 'utente'@'localhost';

-- OP14 – Consultazione di orari e linee
GRANT EXECUTE ON PROCEDURE trasporto_pubblico.sp_op14_consulta_orari
    TO 'utente'@'localhost';


-- === CONDUCENTE: operazioni di servizio ===
DROP USER IF EXISTS 'conducente'@'localhost';
CREATE USER 'conducente'@'localhost' IDENTIFIED BY 'conducente_pwd';

-- Operazioni pubbliche
GRANT EXECUTE ON PROCEDURE trasporto_pubblico.sp_op01_distanza_fermate
    TO 'conducente'@'localhost';
GRANT EXECUTE ON PROCEDURE trasporto_pubblico.sp_op14_consulta_orari
    TO 'conducente'@'localhost';

-- OP02 – Prossima partenza al capolinea
GRANT EXECUTE ON PROCEDURE trasporto_pubblico.sp_op02_prossima_partenza
    TO 'conducente'@'localhost';

-- OP12 – Avanza fermata
GRANT EXECUTE ON PROCEDURE trasporto_pubblico.sp_op12_avanza_fermata
    TO 'conducente'@'localhost';

-- OP13 – Prossime N fermate
GRANT EXECUTE ON PROCEDURE trasporto_pubblico.sp_op13_prossime_fermate
    TO 'conducente'@'localhost';


-- === GESTORE: gestione amministrativa ===
DROP USER IF EXISTS 'gestore'@'localhost';
CREATE USER 'gestore'@'localhost' IDENTIFIED BY 'gestore_pwd';

GRANT EXECUTE ON PROCEDURE trasporto_pubblico.sp_op03a_validazione_biglietto
    TO 'gestore'@'localhost';
GRANT EXECUTE ON PROCEDURE trasporto_pubblico.sp_op03b_validazione_abbonamento
    TO 'gestore'@'localhost';
GRANT EXECUTE ON PROCEDURE trasporto_pubblico.sp_op04_assegna_veicolo_tratta
    TO 'gestore'@'localhost';
GRANT EXECUTE ON PROCEDURE trasporto_pubblico.sp_op05_assegna_conducente_veicolo
    TO 'gestore'@'localhost';
GRANT EXECUTE ON PROCEDURE trasporto_pubblico.sp_op06a_inserisci_conducente
    TO 'gestore'@'localhost';
GRANT EXECUTE ON PROCEDURE trasporto_pubblico.sp_op06b_aggiorna_conducente
    TO 'gestore'@'localhost';
GRANT EXECUTE ON PROCEDURE trasporto_pubblico.sp_op07_orario_crud
    TO 'gestore'@'localhost';
GRANT EXECUTE ON PROCEDURE trasporto_pubblico.sp_op08a_emetti_biglietti_lotto
    TO 'gestore'@'localhost';
GRANT EXECUTE ON PROCEDURE trasporto_pubblico.sp_op08b_emetti_abbonamento
    TO 'gestore'@'localhost';
GRANT EXECUTE ON PROCEDURE trasporto_pubblico.sp_op09_veicolo
    TO 'gestore'@'localhost';
GRANT EXECUTE ON PROCEDURE trasporto_pubblico.sp_op10_fermata
    TO 'gestore'@'localhost';

GRANT EXECUTE ON PROCEDURE trasporto_pubblico.sp_op11_aggiungi_fermata_tratta
    TO 'gestore'@'localhost';

-- =====================================================
-- 6) EVENTI
-- =====================================================

SET GLOBAL event_scheduler = ON;

DELIMITER $$

DROP EVENT IF EXISTS ev_pulizia_titoli_vecchi $$
CREATE DEFINER=`sp_owner`@`localhost` EVENT ev_pulizia_titoli_vecchi
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP + INTERVAL 1 DAY
DO
BEGIN
  DELETE FROM Titolo
  WHERE
    (scadenza IS NOT NULL AND scadenza < (CURDATE() - INTERVAL 365 DAY))
    OR
    (ultimo_utilizzo IS NOT NULL AND ultimo_utilizzo < (CURDATE() - INTERVAL 365 DAY));
END $$

DELIMITER ;


