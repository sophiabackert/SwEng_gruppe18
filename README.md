# Mad Machines – Zentrale Dokumentation & Projektüberblick

Mad Machines ist ein physikbasiertes 2D-Puzzlespiel, bei dem Maschinenkomponenten kreativ kombiniert werden, um einen Ball ins Ziel zu bringen. Dieses Repository enthält nicht nur den vollständigen Quellcode, sondern auch eine **lückenlose, strukturierte Projektdokumentation**. Die README ist der zentrale Einstiegspunkt und verweist auf alle relevanten Doku-Bereiche.

---

## Dokumentations-Überblick

| Bereich                        | Inhalt & Qualitätshinweis                                                                 | Link/Datei                                  |
|--------------------------------|-----------------------------------------------------------------------------------------|---------------------------------------------|
| **1. Anforderungen**           | Vollständige Projektanforderungen, Usecase-Diagramm, Storycards                         | [Anforderungsdokument (PDF)](docs/Anforderungsdokument.pdf), [Story Cards (PDF)](docs/Story%20Cards.pdf) |
| **2. Architektur**             | Ausführliche, begründete Architektur-Dokumentation, Klassendiagramm     | [Architektur & Struktur](docs/Projektstruktur_Architektur.md), [Klassendiagramm](docs/Klassendiagramm.png) |
| **3. Bedienungsanleitung**     | Schritt-für-Schritt-Anleitung, Steuerung, Tipps, Fehlerbehebung                         | [Benutzeranleitung](docs/Benutzeranleitung.md) |
| **4. Javadoc**                 | Vollständige API-Dokumentation (generiert, HTML)                                        | [Javadoc-Startseite](docs/javadoc/apidocs/index.html) |
| **Sanity Checks & Qualität**   | Build-/Testnachweise, Tool-Checks, CI, Testabdeckung                                    | [Sanity Checks](docs/sanity_checks.md)      |

---

## 1. Anforderungen (Usecases & Storycards)
- **Anforderungsdokument**: Enthält alle funktionalen und nicht-funktionalen Anforderungen, Usecase-Diagramm, Akzeptanzkriterien und geplante Features.
- **Story Cards**: Nutzerzentrierte Beschreibung aller Kernfunktionen, mit Akzeptanzkriterien.
- **Direktzugriff:**
  - [Anforderungsdokument (PDF)](docs/Anforderungsdokument.pdf)
  - [Story Cards (PDF)](docs/Story%20Cards.pdf)

## 2. Architektur-Dokumentation
- **Projektstruktur & Architektur**: Ausführlich begründet, mit Layer-Übersicht, Kommunikationswegen, Erweiterbarkeits-Argumenten und echten Beispielen aus dem Code.
- **Klassendiagramm**: Visualisierung der Klassenstruktur und Beziehungen.
- **Stand:** Immer aktuell, direkt aus der Implementierung abgeleitet.
- **Direktzugriff:**
  - [Architektur & Struktur (Markdown)](docs/Projektstruktur_Architektur.md)
  - [Klassendiagramm (PNG)](docs/Klassendiagramm.png)

## 3. Bedienungsanleitung
- **Benutzeranleitung**: Schritt-für-Schritt-Einstieg, Steuerung, Level-Editor, Tipps & Fehlerbehebung.
- **Direktzugriff:**
  - [Benutzeranleitung (Markdown)](docs/Benutzeranleitung.md)

## 4. Javadoc-Dokumentation
- **API-Referenz**: Vollständige, automatisch generierte Javadoc für alle Java-Klassen und Pakete.
- **Direktzugriff:**
  - [Javadoc-Startseite (HTML)](docs/javadoc/apidocs/index.html)
- **Hinweis:** Die Javadoc kann lokal mit Maven generiert werden (`mvn javadoc:javadoc`).

---

## Weitere Nachweise & Qualitätssicherung
- **Sanity Checks**: Dokumentation aller Build-, Test- und Tool-Checks, inkl. CI, Testabdeckung und Tool-Validierung.
  - [Sanity Checks (Markdown)](docs/sanity_checks.md)

---

## Team & Rollen
| Rolle                     | Person   | Hauptaufgaben                               |
|---------------------------|----------|--------------------------------------------|
| **Architektur & Physik**   | Niklas   | Softwarearchitektur, jBox2D-Integration, Core-Engine |
| **Testing & Projektleitung** | Sophia   | JUnit-Tests, CI-Pipeline, Zeitplancontrolling, Kommunikation |
| **GUI & Leveldesign**      | beide    | JavaFX-UI, JSON-Level-Format, Level-Balancing |
| **Dokumentation**          | beide    | Requirements, UML, User-Manual, Präsentation |

---

## Hinweise zur Navigation
- **Alle Dokumente befinden sich im `docs/`-Verzeichnis.**
- **Javadoc**: Öffne die `index.html` im Browser für die API-Referenz.
- **Architektur, Benutzeranleitung, Sanity Checks**: Markdown-Dateien, direkt lesbar oder in PDF konvertierbar.

---

## Technische Hinweise
- **Build & Test:**
  - Kompilieren: `mvn clean compile`
  - Starten: `mvn javafx:run` oder Main.java in der IDE
  - Tests: `mvn test`
  - Javadoc: `mvn javadoc:javadoc`
