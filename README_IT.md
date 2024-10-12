## Readme
[README: English](./README.md)

[README: Italiano](./README_IT.md)


# Mod TeleportBlock

## Panoramica
La Mod TeleportBlock è una mod di Minecraft per Fabric che permette agli amministratori di creare collegamenti di teletrasporto tra due blocchi utilizzando un oggetto honeycomb. I giocatori possono essere teletrasportati calpestando uno dei blocchi collegati. Solo gli amministratori hanno il permesso di creare e rimuovere collegamenti di teletrasporto.

## Caratteristiche
- Imposta collegamenti di teletrasporto tra due blocchi
- Solo gli amministratori possono creare e rimuovere collegamenti di teletrasporto
- I giocatori verranno teletrasportati quando calpesteranno un blocco collegato
- Supporta i permessi e le stringhe di linguaggio configurabili

## Installazione
1. Posiziona il file JAR della mod nella cartella `mods` della tua directory di installazione di Minecraft.
2. Avvia il gioco e la mod genererà automaticamente i file di configurazione necessari.

## Comandi

### `/tport set1 <nome>`
- Usa questo comando dopo aver selezionato il primo blocco con l'honeycomb per impostare il nome del primo blocco (Blocco A).

### `/tport set2 <nome>`
- Usa questo comando dopo aver selezionato il secondo blocco con l'honeycomb per impostare il nome del secondo blocco (Blocco B) e completare il collegamento di teletrasporto tra Blocco A e Blocco B.

### `/tport cancel`
- Annulla l'impostazione del teletrasporto corrente.

### `/tport reload`
- Ricarica i collegamenti di teletrasporto e i file di configurazione del linguaggio.

### `/tport help`
- Mostra informazioni di aiuto sull'utilizzo della mod, inclusi i passaggi per impostare i collegamenti di teletrasporto.

## Utilizzo

![utilizzo](https://raw.githubusercontent.com/Cubolico/teleport-block-mod/refs/heads/main/gif-example/usage.gif)

1. Seleziona il primo blocco (Blocco A) facendo clic destro su di esso con l'honeycomb.
2. Esegui il comando `/tport set1 <nome>` per impostare il nome per il Blocco A.
3. Seleziona il secondo blocco (Blocco B) facendo clic destro su di esso con l'honeycomb.
4. Esegui il comando `/tport set2 <nome>` per collegare Blocco A e Blocco B.
5. Una volta collegati, i giocatori che calpesteranno un blocco saranno teletrasportati all'altro.

## Configurazione
- La mod genera automaticamente una directory di configurazione in `config/teleportblock` con due file importanti:
  - `teleport_links.json`: Contiene i dati dei collegamenti di teletrasporto.
  - `language.txt`: Contiene stringhe di linguaggio personalizzabili per i messaggi della mod.

## Permessi
- Solo i giocatori con permessi di livello admin (livello 4) possono creare e rimuovere collegamenti di teletrasporto.
- I giocatori non amministratori non possono rompere i blocchi collegati al teletrasporto.

## Personalizzazione del Linguaggio
- Puoi personalizzare i messaggi della mod modificando il file `language.txt`. Ogni riga corrisponde a un messaggio specifico nella mod, e puoi modificarli per adattarli alla lingua o allo stile del tuo server.
