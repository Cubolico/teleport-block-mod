## Readme
[README: English](./README.md)

[README: Italiano](./README_IT.md)


# Teleport Mod

**Teleport Mod** è una mod server side per Minecraft che consente ai giocatori di collegare due segnaletiche e teletrasportarsi tra di esse. È progettata per l'uso su server multiplayer e consente agli amministratori di configurare e gestire i collegamenti di teletrasporto.

## Funzionalità

- Collega due segnaletiche di legno utilizzando l'ossidiana per creare un collegamento di teletrasporto.
- Solo gli amministratori con un livello di permesso configurabile possono configurare o distruggere i collegamenti.
- I giocatori possono utilizzare i cartelli collegati per teletrasportarsi da un punto all'altro.
- Comando `/teleportmod reload` per ricaricare la configurazione e il file di lingua senza riavviare il server.

## Requisiti

- Minecraft 1.21 o superiori
- Fabric API

## Installazione

1. Posiziona il file `.jar` della mod nella cartella `mods` del server Minecraft.
2. Avvia il server per generare i file di configurazione.

## Configurazione

Una volta avviato il server, verranno generati i file di configurazione nella cartella `config/teleportmod/`:

- **config.json**: File di configurazione dove puoi impostare il livello di permesso necessario per creare o distruggere collegamenti di teletrasporto.
- **language.txt**: File di lingua dove puoi personalizzare i messaggi mostrati ai giocatori.

### Configurazione `config.json`

Il file `config.json` include una proprietà per il livello di permesso degli amministratori:

```json
{
  "permissionLevel": 4
}
```

- `permissionLevel`: Specifica il livello minimo di permesso richiesto per creare o distruggere collegamenti di teletrasporto. Il livello di permesso di default è `4` (amministratore).

### Configurazione `language.txt`

Il file `language.txt` consente di personalizzare i messaggi che i giocatori vedono durante l'utilizzo della mod. Ecco un esempio di come appare:

```vbnet
sign_a_selected=Cartello A selezionato!
teleport_link_set=Collegamento di teletrasporto creato tra A e B!
teleported_to=Teletrasportato a
no_permission_to_destroy=Non hai il permesso per distruggere questo cartello!
error_already_linked=Errore: Uno dei cartelli è già collegato!
```

Puoi modificare i messaggi a tuo piacimento, mantenendo la struttura `chiave=valore`.

## Come Usare la Mod

![usage](https://raw.githubusercontent.com/nemmusu/teleportmod/refs/heads/main/gif-example/usage.gif)

1. **Creare un collegamento di teletrasporto**:
   - Equipaggia un blocco di ossidiana nella mano principale.
   - Fai clic destro su una segnaletica di legno. Questo selezionerà il primo punto di teletrasporto.
   - Fai clic destro su una seconda segnaletica di legno per collegare i due punti.

2. **Teletrasportarsi**:
   - Una volta creato il collegamento, qualsiasi giocatore può cliccare su una delle segnaletiche per essere teletrasportato all'altra.

3. **Rimuovere un collegamento di teletrasporto**:
   - Gli amministratori possono distruggere una delle segnaletiche per rimuovere il collegamento. I giocatori senza il livello di permesso richiesto non possono distruggere i cartelli collegati.

4. **Ricaricare la configurazione**:
   - Usa il comando `/teleportmod reload` per ricaricare i file di configurazione e lingua senza dover riavviare il server.

## Comandi

- `/teleportmod reload`: Ricarica il file di configurazione e il file di lingua.

## Note

- Assicurati che i giocatori abbiano il livello di permesso corretto per utilizzare le funzionalità amministrative della mod (configurabile tramite `config.json`).
- Non dimenticare di fare backup del file `teleport_links.json` per preservare i collegamenti di teletrasporto creati.





