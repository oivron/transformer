# Transformer Change Log

All notable changes to Transformer will be documented in this file.

## [0.9.2] - desember 2008

### Added
* Produserer Aftenposten i tillegg til TVGuiden. Aftenposten produseres uten E24 og Fotballen.
* Programmet installeres nå ved hjelp av et installasjonsprogram.

## [0.9.21] - desember 2008

### Added
* Fikset problem som gjorde at Aftenposten ikke ble produsert med kategorien Sport.

## [0.9.3] - januar 2009

### Added
* Mer stabil metode for å laste ned avis-feedene.
* Mulig å laste ned og produsere både Aftenposten og E24.
* Funksjon for å finne nye uniqueNames og legge disse inn i ønsket kategori (Innenriks, Utenriks, Meninger, Økonomi, osv).
* Nytt stilark (CSS) (forslag) som gir litt bedre visuell opplevelse på skjerm.

### Known problems
* Programmet skriver til mappene Input og Output under \Programfiler\Bojo. For at det skal fungere, må bruker ha skriverettigheter til disse mappene.

## [1.0.0] - mars 2009

### Added
* Viser artikkel for hvert nytt uniqueName slik at det blir enklere å velge riktig kategori.
* Lagt inn nye kanaler + fikset noen gamle kanaler i TVGuiden.
* Kategorier i Aftenposten (Innenriks, Utenriks, Sport, osv.) uten artikler skrives ikke ut.
* Forbedret metode for merging av feeder.
* Fikset manglende norske tegn i kategorioverskriftene for Aftenposten (Okonomi, Miljo, Vaer).
* Dokumenttittel endret i resultatdokumentet for Aftenposten.
* Fikset scrolleproblem og oppdatert visning i statusvinduet.
* Småjusteringer i grensesnittet.

## [1.1.0] - mars 2009

### Added
* Produserer RadioGuiden for 7 kanaler.
* Småfiks i grensesnittet gjør det enklere å bruke tastatur.

## [1.2.0] - juni 2009

### Added

* Ny struktur (tre nivåer i Aftenposten, 4 nivåer i E24).
* Rutine for å oppdatere gammel versjon av uniqueNames.xml (husk backup av gammel versjon før oppgradering av Transformer). Den nye uniqueNames.xml har en annen struktur enn den gamle.
* Mulig å ignorere uniqueNames som ikke skal produseres.
* Forbedret visning av journalist id-post i Aftenposten/E24. Viser riktig når flere journalister.
* Aftenposten/E24: forbedret stilark. Tar hensyn til ulik struktur i Aftenposten og E24.
* Kan åpne uniqueNames.xml og vise struktur på kategoriene (ikke mulig å redigere).
* Innstillinger-dialog gir mulighet til å endre tittel og beskrivelse for Aftenposten/E24.

## [1.2.5] - august 2009

### Added
* Økt programminnet.
* Enkelt hjelpesystem.
* Fikset bug i transformeringen av Aftenposten/E24 (Kommentarer i E24).
* Aftenposten/E24-mal gir output i valid DTBook-format for produksjon i DAISY Pipeline.
* Mal for RadioGuiden og TVGuiden gir output i valid XHTML 1.0. Kan konverteres til DTBook i Pipeline og deretter produseres.
* Tatt ut Fotball siden den likevel ikke brukes.
* Småfiks i grensesnittet.

## [1.2.5.1] - februar 2010

### Added
* Endret nedlasting av TVGuiden til mappen rtvFeedNLB. Det vil gi en sikrere nedlasting av TV-programmet for de 8 dagene som inngår i TVGuiden.

## [2.0.0] - januar 2010

### Added
* Nytt grensesnitt.
* Forbedret statusinformasjon.
* Mulig å lagre uniqueNames.xml i ønsket mappe (må eksistere på forhånd). Flere brukere kan da bruke og bygge opp samme uniqueNames.xml (Aftenposten).
* Bedre håndtering av spesielle tegn forhindrer krasj i programmet (Aftenposten).
* Bedre metode for å finne nye uniqueNames. Programmet validerer disse for å hindre at programmet finner ikke-eksisterende uniqueNames (Aftenposten).
* Validerer ouput-fila (DTBook) fra Aftenposten. Artikler som ikke validerer fjernes automatisk.
* Programmet kjører på de fleste Windows-versjoner. Testet på Windows XP, Vista og 7 (64 bit).

### Known problems
* Det virker som programmet har problemer med filbaner som inneholder æ, ø og å.

## [2.1.0] - mars 2010

### Added

* Utvidet med TVGuiden og RadioGuiden.
* Endret xml:lang-attributtet på dtbook-elementet fra "nb-NO" til "NO" i resultatfila (Aftenposten).
* Metode som viser hvilke mappeområder som programmet bruker (Hjelp - Vis filbaner).
* Små justeringer i grensesnittet.

### Known problems
* Det virker som programmet har problemer med filbaner som inneholder æ, ø og å.
* Dokumentvinduet i programmet viser ikke alltid æ, ø og å riktig. Problemet gjelder bare dokumentvinduet, ikke filene som brukes i produksjonen.
* Når man har gjennomført én produksjon og bytter til en annen (eks. Fra TVGuiden til Aftenposten), fungerer ikke hurtigtastene. Løsningen er å klikke i dokumentvinduet.
