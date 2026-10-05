Unified-Menu
============

Menü-Generierung
----------------

Generierung des vereinigten/unified Menüs. Die Idee hier:

Für angemeldete Benutzer sollten sowohl private, wie auch öffentliche Seiten
in einem Menü zur Verfügung stehen. Einträge mit gleicher friendly URL werden
dabei als "identisch" angesehen und ersetzen dabei öffentliche Seiten.

Manuelle Schritte für die Installation:

- in allen Seiten die bearbetiet werden sollen muss ein Menü mit dem Namen
  "UnifiedMenu" eingerichtet werden
- das Menü sollte als "Hauptnavigation" markiert werden, wenn das Menü gemäß
  dem Beispiel `src/main/site/conditional_menu_example.md` genutzt werden soll

Administrative Eintriffsmöglichkeiten:

- das Menü kann manuell neugebaut werden, durch manuelles Triggern des
  periodischen Rebuilds. Kontrollbereich -> Konfiguration -> Ausführung ->
  Geplante Aufgaben -> MenuRebuildJobConfiguration


404-Handling
------------

Es wird ein spezieller 404 Handler (vgl. `NotFoundInfoRenderer.java`)
installiert, welcher für 404 Seiten prüft, ob es bei Seiten in public (kein
Präfix bzw. `/web/<site>`) und private Pages (Präfix `/group/<site`) im jeweils
anderen Bereich eine Seite mit gleicher FriendlyURL existiert. Ist dies der Fall
so wird diese Seite verlinkt.