# **Java Swing & Sockets Kliens-Szerver Chat Alkalmazás**

Ez egy nulláról felépített, komplett kliens-szerver chat alkalmazás, Java nyelven. Tartalmaz egy Swing alapú grafikus klienst és egy többszálú (multi-threaded) szervert, amely több egyidejű kapcsolatot is képes kezelni.

A projekt elsődleges célja a Java hálózatkezelés, a többszálúság és a GUI programozás alapkoncepcióinak megértése és gyakorlása volt.

---

## **Fontos közlemény (Disclaimer)**

Ez egy tanulási célú projekt, és nem alkalmas éles (production) használatra. Számos ismert korlátja és javítandó területe van (lásd a *Jövőbeli Fejlesztési Lehetőségek* szekciót). A fejlesztés során AI-eszközök segítségét is igénybe vettem, elsősorban architekturális tanácsokhoz, hibakereséshez. A fő fókusz a technikai koncepciók implementálásán és összekötésén volt.

---

## **Funkciók**

**Kliens-Szerver Architektúra:** Többszálú szerver, amely minden logikát és adatot kezel.  
**Felhasználói Regisztráció:** Új felhasználók regisztrálhatnak. A jelszavak validálva vannak (hossz, nagybetű, szám), és jBCrypt hashing segítségével, biztonságosan vannak tárolva.  
**Felhasználói Bejelentkezés:** A regisztrált felhasználók bejelentkezhetnek.  
**1-1 Valós Idejű Chat:** A kliensek valós időben kommunikálhatnak.  
**Barát Rendszer:** A felhasználók felhasználónév alapján hozzáadhatnak másokat egy perzisztens barátlistához.  
**Perzisztens Chat Előzmények:** Minden beszélgetés elmentésre kerül a szerveren, és betöltődik a chat megnyitásakor.  
**Perzisztens Fiókok:** A felhasználói adatok (fiókok, barátlisták) Java Szerializációval vannak elmentve.  
**ngrok Kész:** A kliens úgy van kialakítva, hogy bekérje a Hosztnevet és a Portot is, így könnyen használható ngrok-hoz hasonló "alagút" szolgáltatásokkal (megkerülve a router port forwardolás szükségességét).

---

## **Az Architektúra Felépítése**

Az alkalmazás valójában két különálló programra oszlik: a **ChatServer**-re és a **ChatApp**-ra (a kliens).

---

### **1. A Szerver (ChatServer.java)**

A szerver egy grafikus felület nélküli konzolalkalmazás, amely a központi "agy" szerepét tölti be.

**Hálózatkezelés:**  
`java.net.ServerSocket`-et használ egy adott porton (pl. 12345) való hallgatózásra.

**Többszálúság:**  
Amint egy új kapcsolat beérkezik (`socket.accept()`), azonnal elindít egy új `ClientHandlerThread` szálat, hogy az kezelje az adott klienst. Ez lehetővé teszi, hogy a szerver egyszerre több klienst szolgáljon ki anélkül, hogy leblokkolna.

**Hitelesítés (AuthManager.java):**
- Kezeli a `users.dat` fájlt.  
- Végzi a regisztrációs logikát, validálja az új fiókokat.  
- jBCrypt segítségével "hasheli" és "sózza" (salt) a jelszavakat mentés előtt.  
- `BCrypt.checkpw()` segítségével ellenőrzi a bejelentkezési adatokat.  
- Kezeli a barátok hozzáadását a felhasználók listájához.

**Perzisztencia (MessageStorage.java):**
- `ChatMessage` objektumokat ment külön fájlokba beszélgetésenként (pl. `Anna_Bence_chat.dat`).  
- Betölti a chat előzményeket, amikor egy `HistoryRequest` érkezik a klienstől.

**Protokoll:**  
A szerver és a kliens szerializált Java objektumok küldésével kommunikál (pl. `LoginRequest`, `ServerResponse`, `ChatMessage`).

---

### **2. A Kliens (ChatApp.java)**

A kliens egy Java Swing alapú GUI alkalmazás, amelyet a felhasználók futtatnak a saját gépükön.

**GUI:**  
`CardLayout`-ot használ a panelek közötti váltásra (`Login`, `Register`, `Chat`).

**Hálózatkezelés (NetworkClient.java):**
- Egy külön háttérszálon (`Runnable`) fut, hogy megakadályozza a Swing GUI (Event Dispatch Thread) lefagyását.  
- `java.net.Socket` segítségével csatlakozik a szerverhez.  
- A `while(running)` ciklusában vár a szervertől érkező objektumokra (`ois.readObject()`).  
- **GUI Frissítések:** Amikor a `NetworkClient` (a háttérszál) adatot kap (pl. egy új `ChatMessage`-et), soha nem frissíti közvetlenül a GUI-t. `SwingUtilities.invokeLater()` segítségével biztonságosan átadja az adatot az Event Dispatch Thread-nek, amely aztán frissíti a `JTextArea`-t.

---

## **Felhasznált Technológiák**

- **Mag:** Java (JDK 17)  
- **GUI:** Java Swing  
- **Hálózatkezelés:** Java Sockets (`ServerSocket`, `Socket`)  
- **Párhuzamosság:** Thread, Runnable, volatile, synchronized  
- **I/O:** ObjectInputStream / ObjectOutputStream (Java Szerializáció)  
- **Biztonság:** jBCrypt a jelszó hasheléshez  
- **Build:** Apache Maven (függőségkezelés és csomagolás)

---

## **Használati Útmutató**

### **1. Szerver Oldali Lépések (Neked)**

**Szerver Indítása:**  
Indítsd el a `ChatServer.java` main metódusát az IDE-ből vagy terminálból.

**Szerver Elérhetővé Tétele:**  
A szervered most a `localhost:12345` címen fut. Ahhoz, hogy a barátaid az interneten keresztül elérjék, két lehetőséged van:

**A) Opció (Router):**  
Lépj be a routered admin felületére, és állíts be **Port Forwarding-ot** (Port Továbbítás): minden külső TCP forgalmat a 12345-ös portról irányíts át a te géped lokális IP címére (pl. `192.168.1.10`), szintén a 12345-ös portra.

**B) Opció (Nincs router hozzáférés):**
- Töltsd le és indítsd el az **ngrok**-ot.  
- Futtasd a terminálban: ngrok tcp 12345

**Az ngrok adni fog egy nyilvános URL-t** (pl. `0.tcp.ngrok.io`) **és egy Portot** (pl. `19876`).  

---

## **2. Kliens Oldali Lépések**

**.jar Fájl Építése:**  
Futtasd a `mvn clean package` parancsot a projekt termináljában.  
Ezzel létrejön egy `SzuperChat.jar` fájl a `/target` mappában.  

**.jar Elküldése:**  
Ezt az egy `.jar` fájlt küldd el a barátaidnak.  

**Követelmény:**  
A barátaidnak **Java 17 (vagy újabb)** verzióval kell rendelkezniük.  

**Futtatás:**  
Elindíthatják a `.jar`-t (pl. `java -jar SzuperChatKliens.jar`).  

---

## **Csatlakozás**

Az alkalmazás két felugró ablakot fog mutatni.  

**Ha az A) Opciót (Port Forwarding) használod:**  
- **Hosztnév:** A te Publikus IP címed (nézd meg: [whatismyip.com](https://whatismyip.com))  
- **Port:** `12345`  

**Ha a B) Opciót (ngrok) használod:**  
- **Hosztnév:** Az ngrok által adott URL (pl. `0.tcp.ngrok.io`)  
- **Port:** Az ngrok által adott Port (pl. `19876`)  

---

**Chat:**  
Most már regisztrálhatnak, bejelentkezhetnek, felvehetnek téged barátnak, és kezdődhet a csevegés!  

---

## **Jövőbeli Fejlesztési Lehetőségek (TODO)**

Ez a projekt még közel sincs kész, így számos funkció hiányzik egy valós alkalmazáshoz.  

- [ ] **Valódi Barátkérés:** Egy „függőben” (pending) státusz bevezetése a barátkéréseknél, amit el kell fogadni vagy elutasítani.  
- [ ] **„Ki van online?” Lista:** A szerver tudja, kik vannak bejelentkezve; ezt a listát elküldhetné a klienseknek.  
- [ ] **Biztonság:** A `.dat` fájlok titkosítás nélkül vannak a szerveren.  