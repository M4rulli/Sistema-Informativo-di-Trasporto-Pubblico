# Progetto Basi di Dati  
## Sistema Informativo di Trasporto Pubblico

Applicazione **Java** con persistenza su **MySQL** per la gestione e consultazione di un sistema di trasporto pubblico urbano.  

---

## Requisiti

- **JDK 19+** (consigliato: versione usata nello sviluppo)
- **MySQL 8.0+**
- **Maven** (consigliato)
- Driver JDBC MySQL: `mysql-connector-j`

---

## Setup Database

1. Apri MySQL Workbench e connettiti al tuo server MySQL.
2. Importa lo script SQL del progetto (`sql/database.sql`):
   - **Server > Data Import** oppure esegui manualmente lo script.
3. Verifica che lo script crei:
   - schema + tabelle
   - trigger/eventi
   - utenti DB e privilegi (GRANT EXECUTE sulle procedure)

> Nota: il progetto presuppone che le procedure esistano e siano invocabili dagli utenti DB dedicati.

---


## Compilazione ed Esecuzione

### Con Maven
1. Compila il progetto:
   ```bash
   mvn clean compile
   ```
2. Avvia l’applicazione:
   ```bash
   mvn exec:java
   ```

---

## Pulizia della compilazione

Per eliminare i file generati dalla compilazione precedente ed effettuare una compilazione pulita, esegui:

```bash
mvn clean
```

Questo comando rimuove la cartella `target` e tutti i file compilati.

---
