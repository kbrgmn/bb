---
title: BurgmannBuchhaltung Dokumentation
subtitle: Verrechnungsmöglichkeiten und Preisbildungsmodelle im **Modul Automatisierte Verrechnung**
lang: "de"
page-background: "/home/gatgeagent/.local/share/pandoc/templates/examples/page-background/backgrounds/background5.pdf"

titlepage: true,
titlepage-rule-color: "360049"
titlepage-background: "/home/gatgeagent/.local/share/pandoc/templates/examples/title-page-background/backgrounds/background7.pdf"

date: "Q2 2024"

toc: true
toc-own-page: true

output: pdf_document

author:
  - BurgmannSystems
  - Burgmann Accounting Plus

colorlinks: true
header-includes:
  - |
    ```{=latex}
    \usepackage{awesomebox}
    ```
pandoc-latex-environment:
  noteblock: [note]
  tipblock: [tip]
  warningblock: [warning]
  cautionblock: [caution]
  importantblock: [important]
---

# Moduldokumentation Automatisierte Verrechnung für BurgmannBuchhaltung

::: note
Do you require this document in a different language?
Please do not hesitate to contact BurgmannSystems or Burgmann Accounting Plus with your requirements.
We will gladly provide you with translations in English, French, or any other language.
:::

## Übersicht

Im folgenden Dokument finden Sie die verschiedenen Arten der Verrechnungsmodelle
sowie einen Auszug an möglichen Preisbildungsmodellen, welche Sie mit dem Modul
Automatisierte Verrechnung konfigurieren können.\
Bitte beachten Sie, dass für das Modul Automatisierte Verrechnung eine
zusätzliche Lizenz und ein aufrechtes BurgmannBuchhaltung-Abonnement benötigt
werden.

## Disclaimer

Die folgenden Informationen beziehen sich auf den geplanten Stand der
Entwicklung. Möglicherweise sind noch nicht alle genannten Punkte verfügbar.

## Einleitung

Das Modul Automatisierte Verrechnung ist ein Zusatzmodul, mit welcher Sie Ihre
Instanz von BurgmannBuchhaltung mit einer Vielfalt von Funktionen bezüglich
Ereignisaufzeichnung und -verrechnung aufwerten können, welche es ermöglichen,
verschiedene komplexe Verrechnungsvorgänge vollautomatisiert auszuführen.

Das Modul erlaubt es Ihnen, Ereignisse und ähnliche Verrechnungsvorgänge mittels
einer API aufzunehmen und automatisch in BurgmannBuchhaltung zu verbuchen. Für
die Nutzung dieser API werden Ihnen eine Reihe von dazugehörigen SDKs für verschiedene
Programmiersprachen angeboten.

Das Modul wurde extra an die Bedürfnisse moderner Unternehmen zugeschnitten,
welche ihren Kunden Cloud- oder SaaS-Dienstleistungen (Software-as-a-Service)
anbieten.

\pagebreak

## Vorteile

- Verrechnen Sie Ihren Kunden vollautomatisch: einzelne Funktionsnutzungen,
  API-Aufrufe, genutzte Ressourcen, und vieles mehr - das Modul ist vollkommen
  agnostisch, welche Nutzungen oder Einheiten Sie verrechnen.
- Stellen Sie im einfach zu benutzenden No-Code Webinterface individuelle Tarife zusammen: 
  - legen Sie selbstständig **Freieinheiten und Abrechnungszeiträume** (z.B.
    monatlich, quartalsweise) fest,
  - wählen Sie bei der Abrechnung zwischen: **Prepaid (Guthabenkonto) / Postpaid
    (Zahlung am Ende der Verrechnungsperiode)**
- **Sekundengenaue Abrechnung**, oder geben Sie die Abrechnungszeiträume völlig
  individuell vor, z.B.: virtuelle Server auf Stundenbasis; gespeicherte
  Gigabyte pro Tag; CPU-Zeit in Sekunden.
- Asynchrone Echtzeiterfassung von **zehntausenden Ereignissen pro Sekunde** -
  oder nutzen Sie die Möglichkeit einer nachträgliche Nutzungsdatenauswertung.
- Verschiedenste Preisbildungsmodelle möglich, um Ihrer Finanzabteilung völlige
  Freiheit bei der Vertragsauslegung an Ihre Kunden zu schaffen, z.B.:
  [Staffelpreismodell](#Staffelpreismodell),
  [Volumenpreismodell](#Volumenpreismodell),
  [Paketpreismodell](#Paketpreismodell). Auch hybride Modelle aus Tarifen und
  nutzungsbezogener Verrechnung bieten sich an.
- Sind Sie im B2B2B-Bereich tätig (Ihre Kunden sind nicht die Endverbraucher)?
  Stellen Sie Ihren Geschäftskunden eine fertige Abrechnung zur Verfügung -
  aufgeschlüsselt nach deren (Unter-) Kunden! (hierarchische Tenant-Trennung) -
  welche diese an deren Kunden weiterverrechnen können.
- Sind Sie grenzübergreifend tätig? Die Überregionalitätsfunktionen von
  BurgmannBuchhaltung (z.B. Fremd- & Multiwährungskontierung, autom. Verbuchung v.
  Fremdwährungen nach Zollwertkurs, mehrsprachige Rechnungslegung, EU-weite
  VAT/USt Steuer-Engine, konforme Geschäftskundenvalidierung für EU + weitere,
  uvm.) werden mit dem Modul Automatisierte Verrechnen fortgeführt, so können
  Sie etwa dem oftmals angefragtem Wunsch Ihrer Kunden nachkommen, die
  Verrechnung in dessen Zeitzone zu führen.
- Stellen Sie Ihren Kunden ein Webportal zur Verfügung, mittels welchem diese
  ihre Abrechnungen einsehen und ihre Rechnungen - selbstständig ohne Zutun
  Ihrerseits - begleichen können.
- **Integrieren Sie eine Vielzahl von Zahlungsanbietern** (z.B.: Stripe (USA),
  Mollie (EU)) und verschiedene Zahlungsmethoden (z.B. Kartenzahlung, SEPA-Überweisung,
  SEPA (Firmen-) Lastschrifteinzug).
- Lassen Sie Ihren Kunden automatisch die aktuelle Rechnung zukommen, etwa als
  (auch S/MIME verschlüsselte) **PDF per E-Mail, per Telefax, oder als
  Postbrief**.
- Automatische Verbuchung der Ausgangsrechnungen in BurgmannBuchhaltung.

## Verrechnungsmöglichkeiten

### Ereignisse

Bei Ereignissen handelt es sich um einzelne Zeitpunkte in dem Zeitstrahl
(Verbuchungszeitraum), z.B.:

- Ihr Kunde tätigt einen API-Aufruf bei Ihrer API.
- Ihr Kunde betätigt einen Knopf und nutzt eine Funktion in Ihrer Software.
- Ihr Kunde nutzt eine Einheit in dessem Tarif (z.B. Minuten / MB eines IoT Tarifs).

### Ressourcen

Bei Ressourcen handelt es sich um länger andauernde Zeithorizonte auf dem
Zeitstrahl, welche die Zustände "offen" (nicht abgeschlossen / in Nutzung) oder
"geschlossen" (beende Nutzung) einnehmen können.

Beispiele von Ressourcen:

- Ihr Kunde nutzte für 14 Tage, 12h, und 34 Sekunden eine Cloud-Server-Instanz.
- Ihr Kunde nutzte 34 Minuten Videokonvertierungszeit.
- Ihr Kunde speichert seit 74 Tagen 150 Gigabyte Daten, und hat die Nutzung noch
  nicht beendet.

Im Gegensatz zu Ereignissen, welche immer nur in einem spezifischen
Verrechnungszeitraum eintreten, können Ressourcen über mehrere
Verrechnungszeiträume hinweg bestehen.

### Transaktionen

_Diese Funktion befindet sich in Entwicklung._

Bei Transaktionen handelt es sich um Ereignisse, welche zusätzlich mit einem
Währungswert versehen werden, um die Nutzung der prozentuellen
Preisbildungsmodelle zu ermöglichen. Diese Type wird häufig für
Resellergeschäfte genutzt.

\pagebreak

## Preisbildungsmodelle

Bitte beachten Sie, dass es sich hier lediglich um einen Auszug der gängigen
Preisbildungsmodelle handelt. Sie haben bei der Konfiguration natürlich noch
weitere Freiheiten, sollten Sie ein komplexeres Modell anstreben.

Alle Beispiele werden im Folgenden in EUR abgebildet, sind jedoch auch mit allen
anderen Währungen möglich.

### Fixpreismodell

Das Fixpreismodell ist das einfachste darstellbare Preisbildungsmodell. Hierbei
handelt es sich um einen einzelnen Wert, welcher für alle Ereignisse oder
Einheiten von Ressourcen angewandt wird.

Beispiele:

- Legen Sie einen Fixpreis von EUR 0,07 / API-Aufruf fest, und Ihr Kunde tätigt
  während eines Verrechnungszeitraums 10.000 API-Aufrufe, so werden diesem EUR 700
  verrechnet.
- Legen Sie einen Fixpreis von EUR 0,005 / Sekunde CPU-Zeit fest, und Ihr Kunde
  nutzt 18,5 Stunden CPU-Zeit, so werden diesem EUR 333 verrechnet.

\pagebreak

### Staffelpreismodell

Bei dem Staffelpreismodell wird die Preisbildung in mehrere Staffeln unterteilt,
welche einzeln verrechnet werden, wodurch sich dieses Modell vom
[Volumenpreismodell](#Volumenpreismodell) unterscheidet, bei welchem nur die
letzte Stufe zur Berechnung benutzt wird.

#### Beispiel eines einfachen Staffelpreismodells

##### Konfiguration

|                  | Einheiten von | Einheiten bis | Preis pro Einheit |
| ---------------- | ------------- | ------------- | ----------------- |
| Für die ersten   | 1             | 100           | EUR 5             |
| Für die nächsten | 101           | 1000          | EUR 4             |
| Für die nächsten | 1001          | 5000          | EUR 3             |
| Für die nächsten | 5001+         |               | EUR 1             |

**Berechnungsbeispiel**

| Einheiten | Preis  | Berechnung                                          |
| --------- | ------ | --------------------------------------------------- |
| 0         | 0€     | -                                                   |
| 1         | 5€     | 1 \* 5€                                             |
| 100       | 500€   | 100 \* 5€                                           |
| 101       | 504€   | (100 _ 5€) + (1 _ 4€)                               |
| 1000      | 4100€  | (100 _ 5€ = 500€) + (900 _ 4€ = 3600€)              |
| 5000      | 16100€ | (100 _ 5€) + (900 _ 4€) + (4000 \* 3€)              |
| 10000     | 21100€ | (100 _ 5€) + (900 _ 4€) + (4000 _ 3€) + (5000 _ 1€) |

\pagebreak

### Volumenpreismodell

Bei dem Volumenpreismodell wird wie beim
[Staffelpreismodell](#Staffelpreismodell) die Preisgebung in verschiedene Stufen
eingeteilt, wobei bei der Evaluierung aber nicht jede Stufe mit der in diese
Stufe hineinfallenden Nutzung berechnet wird, sondern ausschließlich die letzte
zutreffende Preisstufe evaluiert wird.

#### Beispiel eines einfachen Volumenpreismodells

##### Konfiguration

|     | Einheiten  | Preis           |
| --- | ---------- | --------------- |
| Ab  | 100 Stück  | nur 17€ Stück   |
| Ab  | 500 Stück  | nur 15€ / Stück |
| Ab  | 1000 Stück | nur 12€ / Stück |

**Berechnungsbeispiel**

| Einheiten | Preis     | Berechnung  |
| --------- | --------- | ----------- |
| 450       | EUR 7650  | 450 \* 17€  |
| 500       | EUR 7500  | 500 \* 15€  |
| 1000      | EUR 12000 | 1000 \* 12€ |

Wie Sie in diesem Beispiel sehen, kann beim Volumenpreismodell der Gesamtpreis
niedriger werden, wenn man durch die zusätzliche Nutzung von Einheiten in die
nächste Preiskategorie fällt.

\pagebreak

### Paketpreismodell

Das Paketpreismodell kombiniert den Preis mit einer gewissen Anzahl an
Einheiten. Zum Beispiel:

- €250 per 100 Anfragen
- $0.01 per 1000 AI-Tokens

#### Konfiguration

Es wird ein offen-endendes Paket mit Preisstufengebühr von _EUR 100 pro 1.000
Einheiten_ konfiguriert.

**Berechnungsbeispiel**

| Einheiten | Preis   | Berechnung           |
| --------- | ------- | -------------------- |
| 1         | EUR 100 | 1x Paket i.H.v. 100€ |
| 1000      | EUR 100 | 1x Paket i.H.v. 100€ |
| 1001      | EUR 200 | 2x Paket i.H.v. 100€ |
| 1500      | EUR 200 | 2x Paket i.H.v. 100€ |
| 2000      | EUR 200 | 2x Paket i.H.v. 100€ |
| 2001      | EUR 300 | 3x Paket i.H.v. 100€ |

\pagebreak

### Staffelpaketpreismodell

| Einheiten von | Einheiten bis | Paketpreis | Stück         |
| ------------- | ------------- | ---------- | ------------- |
| 1             | 1000          | EUR 100    | per 100 Stück |
| 1001          | 5000          | EUR 100    | per 250 Stück |
| 5001+         |               | EUR 100    | per 500 Stück |

**Berechnungsbeispiel**

| Einheiten | Preis | Berechnung                                                              |
| --------- | ----- | ----------------------------------------------------------------------- |
| 1         | 100€  | 1x Paket i.H.v. 100€ / 100 Stk.                                         |
| 100       | 100€  | 1x Paket i.H.v. 100€ / 100 Stk.                                         |
| 101       | 200€  | 2x Paket i.H.v. 100€ / 100 Stk.                                         |
| 500       | 500€  | 5x Paket i.H.v. 100€ / 100 Stk.                                         |
| 1000      | 1000€ | 10x Paket i.H.v. 100€ / 100 Stk.                                        |
| 1001      | 1100€ | 10x Paket i.H.v. 100€ / 100 Stk. + 1x Paket i.H.v. 100€ / 250 Stk.      |
| 1250      | 1100€ | 10x Paket i.H.v. 100€ / 100 Stk. + 1x Paket i.H.v. 100€ / 250 Stk.      |
| 1251      | 1200€ | 10x Paket i.H.v. 100€ / 100 Stk. + 2x Paket i.H.v. 100€ / 250 Stk.      |
| 5000      | 2600€ | 10x Pkt. 100€ / 100 Stk. (1000€) + 16x Pkt. 100€ / 250 Stk. (1600€)     |
| 5500      | 2700€ | 10x Pkt. 100€/100 Stk. + 16x Pkt. 100€/250 Stk. + 1x Pkt. 100€/500 Stk. |
| 5501      | 2800€ | 10x Pkt. 100€/100 Stk. + 16x Pkt. 100€/250 Stk. + 2x Pkt. 100€/500 Stk. |

### Transaktionsabhängiges prozentuelles Preisbildungsmodell

_Diese Funktion befindet sich in Entwicklung._
