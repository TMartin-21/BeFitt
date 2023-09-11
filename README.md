# BeFitt run tracker- android applikáció

Az alkalmazás célja, hogy a felhasználó képes legyen valós időben rögzíteni az általa megtett útvonalat. Minden rögzített útvonalat és a hozzá tartozó adatokat el lehet menteni egy listába. A célközönség főként sportolók, akik saját, egyedi edzéstervet szeretnének kialakítani maguknak, de akár túrázók számára is hasznos lehet.

## Főbb funkciók

A felhasználó nyomon tudja követni és rögzíteni az általa megtett útvonalat. A rögzített útvonal egy külön listába kerül elmentésre. A lista elemeinek egy details nézete is van, ahol a rögzített adatok tekinthetők meg. A listába nem csak új elemek kerülhetnek bele, de törölni is lehet őket. Minden adat perzisztens módon kerül eltárolásra. Az alkalmazás indításakor egy profil nézet jelenik meg, ahol a felhasználó megadhatja a nevét, testmagasságát és testtömegét, későbbi felhasználásához. Ezt követi az a nézet, ahol az útvonal rögzítés indítható.

A felhasználó útvonalakat rögzíthet, futáshoz, kerékpározáshoz, vagy akár gyalogláshoz. Egy útvonalat a megtett távolsággal, az átlagsebességgel, a hozzá tartozó útvonallal térképen bejelölve, annak kezdő idejével és végével, valamint a kezdettől a befejezésig eltelt idővel lehet leírni. A rögzítés folyamatát "start" gombbal lehet elindítani, "pause" gombbal szüneteltetni és "stop"
-al pedig leállítani. Az útvonal rögzítésének a befejezését követően, a rendszer kiszámolja az égetett kalória mennyiségét, majd a többi adattal együtt el lehet menteni egy listába azt, ahol a megtett útvonalak a kezdő idő szerint rendezetten jelennek meg. A felhasználó be tudja állítani a minimum megtenni kívánt távolságot. Ha ezt a távolságot megtette, a rendszer egy notification-t küld erről. Az útvonal rögzítése GPS helymeghatározás és térképkezelés segítségével történik.

## Felhasználói kézikönyv

Az alkalmazás 5 különböző nézettel rendelkezik. A 3 fő nézet között az alkalmazás indítását követően azonnal lehet váltani, egy alsó navigációs menü segítségével.

Indításkor ezek közül az első nézet a Profil. Itt egy speed dial segítségével 3 opciónk van. Beállíthatjuk a felhasználói adatokat egy edit gomb segítségével, törölhetjük őket, illetve profilképet is beállíthatunk. Az edit gomb megnyomására egy újabb nézet ugrik fel, ahol a szükséges adatokat (név, életkor, magasság, testtömeg, minimum megtenni kívánt távolság) kell megadni, majd egy OK gombbal elmenthetjük azokat.

<p align="center">
    <img src="/Screenshot_profile_framed.png" width="280">
    <img src="/Screenshot_edit_profile_framed.png" width="280">
</p>

A második nézet az, ahol az útvonal rögzítése megtörténik. Itt egy google térkép jelenik meg. A profil nézethez hasonlóan, a funkciók elérését itt is speed dial biztosítja. A speed dial-on 3 lehetséges funkció adott. Start-al elindítjuk a rögzítést, pause-al szüneteltethetjük. Start után a start gomb átvált pause-ra, pause lenyomására pedig visszavált startra, így a félbeszakított rögzítést bármikor folytatni lehet és újra félbeszakítani, egy gomb segítségével. A másik funkció, az elindított útvonal rögzítésének a befejezése és lementése. Ez a stop gomb segítségével lehetséges. A 3. funkció a zene gomb, amely elindítja a Spotify alkalmazást, ha le van töltve a készülékre. Ha nincs, akkor azt egy Snakbar üzenetben jelzi. Az útvonal rögzítése közben különböző adatokat tekinthetünk meg (pillanatnyi sebesség, eltelt idő, pozíció, megtett távolság).

<p align="center">
    <img src="/Screenshot_track_run_framed.png" width="280">
</p>

A harmadik fő nézet a statisztika, ahol a lementett útvonalak sorakoznak egy listában. Minden listaelem egy rögzített útvonal, amelyet a rögzítés kezdetének ideje és a megtett távolság jellemez. További adatok megtekintéséhez a kiválasztott listaelemre kell kattintani, majd egy details nézet jelenik meg. A felhasználó számára részletesebb adatokkal szolgál a details nézet. A különféle adatok: az útvonal rögzítésének kezdete és vége, a ténylegesen (amikor a felhasználó nem szüneteltette a rögzítést) eltelt idő, átlagsebesség, a megtett távolság, és a profilban beállított értékek, illetve az átlagsebesség segítségével kiszámolt égetett kalória mennyisége. Egy google térképen pedig a rögzítéskor megtett teljes útvonal visszanézhető. A listában lévő elemek törölhetők egyesével, hosszú kattintásra, vagy akár egyszerre is, egy Delete All gomb lenyomására, amely a jobb felső sarokban található.

<p align="center">
    <img src="/Screenshot_stat_list_framed.png" width="280">
    <img src="/Screenshot_details_framed.png" width="280">
</p>

## Felhasznált technológiák

Az egész alkalmazás alapja, hogy az útvonalat helymeghatározás segítségével rögzíteni lehessen, és további adatokkal együtt le lehessen menteni. Így az alkalmazás megvalósításához használt technológiák:

- Megjelenítéshez, felhasználói felülethez **activityk, fragmentek** lettek használva

- A fő nézetek közötti navigáláshoz az Android Studio által felkínált **Bottom Navigation Activity** lett használva

- A különböző funkciók eléréséhez egy külső könyvtár által nyújtott [**Speed Dial**](https://github.com/leinardi/FloatingActionButtonSpeedDial) floating action button segít

- Profilkép megjelenítéséhez [**Avatar View**](https://github.com/GetStream/avatarview-android) könyvtár használata

- Helymeghatározás **Fused Location API** segítségével

- **LifecycleService** az útvonal folyamatos monitorozásához

- A serviceben lekért adatokat **Broadcast Intent** továbbítja a TrackRunFragmentnek, amely az adatok megjelenítéséért felel

- **Broadcast Receiver** haszálata, hogy a TrackRunFragment fogadni tudja a service által nyújtott adatokat

- **Veszélyes engedélyek elkérése** futásidőben, a broadcast receiver beregisztrálásakor

- **Notificationök** használata, rendszerüzenetek megjelenítéséhez (track service started, paused, stoped, aktuális pozíció megjelenítése, elért minimum távolság jelzése)

- **RecyclerView** a lementett útvonalak listában való megjelenítéséhez

- **ViewModel** a listához

- **Room** használata a listaelemek perzisztens tárolásához

- **SharedPreferences** a profil adatok perzisztens tárolásához

- **Repositoryk** a perzisztens módon eltárolt adatok eléréséhez, módosításához, törléséhez

- A térkép kezeléséhez **Google Maps API**

- **Stílusok, színek** használata a sötétebb megjelenítéshez, night style google térkép