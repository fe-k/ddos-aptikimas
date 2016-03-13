package dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
* Klasė skirta skaičiavimams su entropija atlikti
* Pavadinta storage, nes ji kaip ir saugo iš duombazės gražintą informciją */
public abstract class StorageInTimeDomain {

    /* Pagrindinis kintamasis, kuris bus naudojamas visuose entropijos skaičiavimuose.
     * Jame saugojama susumuota lango plotyje paketų informacija */
    private Map<String, PacketsInfo> packetCountsGroupedByIp;

    /* Saugoja paketų skaičius vienetiniame lango plotyje */
    private List<PacketsInfo> currentTimePacketCounts;

    /* Saugoja visus paketų skaičius visame lango plotyje */
    private List<List<PacketsInfo>> listOfListOfPacketCounts;

    /* Dabartinis laikas */
    private Long currentTimeInMilis;

    private List<Long> listOfFrom;
    private Long from;
    private int windowWidth;

    private List<ValueInTimeInterval> listOfEntropies;
    private ValueInTimeInterval minimumEntropy;
    private Double notNormalizedCurrentTimeEntropy;
    private Double normalizedCurrentTimeEntropy;

    private Double addToAttackListIfLowerThan;
    private List<ValueInTimeInterval> attackList;

    /*
    * Konstruktorius, nieko daug nedaro, tik inicijuoja masyvus ir nusetina defaultines reikšmes
    * tokias, kaip lango plotis
    * */
    public StorageInTimeDomain() {
        this.packetCountsGroupedByIp = new HashMap<String, PacketsInfo>();
        this.currentTimePacketCounts = new ArrayList<PacketsInfo>();
        this.listOfListOfPacketCounts = new ArrayList<List<PacketsInfo>>();
        this.currentTimeInMilis = 0L;

        this.listOfFrom = new ArrayList<Long>();
        this.from = 0L;
        this.windowWidth = 50;

        this.listOfEntropies = new ArrayList<ValueInTimeInterval>();

        this.minimumEntropy = new ValueInTimeInterval();
        this.minimumEntropy.setValue(1D);

        this.addToAttackListIfLowerThan = 0D;
        this.attackList = new ArrayList<ValueInTimeInterval>();
    }

    /* Metodas, kuris gražina IP, jis abstraktus, nes jeigu kartais norėtumėm grupuoti ne pagal paskirties
    * adresą, o pagal šaltinio adresą, galėsime lengvai pakeisti */
    abstract String getIp(PacketsInfo pc);

    /*
    * Paduodame masyvą paketų informaciją turinčių objektų.
    * Pagal šią informaciją nusprendžiame, nuo kurių IP adresų ir po kiek paketų atimti.
    * */
    private StorageInTimeDomain decreasePacketCounts(List<PacketsInfo> packetCountsToDecrease) {
        /* Einame per visus paketų informaciją saugojančius objektus */
        for (PacketsInfo pc: packetCountsToDecrease) {
            String ip = getIp(pc);

            PacketsInfo countByDestination = packetCountsGroupedByIp.get(ip);
            /* Jeigu paduoto IP adreso nėra, tai nėra nuo ko ir atimti. Šio atvejo neturėtų niekad būti */
            if (countByDestination == null) {
                continue;
            }
            /* Atimame nurodytą paketų skaičių */
            countByDestination.setCount(countByDestination.getCount() - pc.getCount());

            /* Jeigu paketų skaičius pasiekia nulį. IP adreso saugojimas nebeturi prasmės, nes jis
            * neįtakos apskaičiuotos entropijos reikšmės, taigi tiesiog pašalinam */
            if (countByDestination.getCount() == 0) {
                packetCountsGroupedByIp.remove(ip);
            }
        }
        return this;
    }

    /*
    * Priešingas nei "decreasePacketCounts".
    * Tiesiog vėl paduodame masyvą paketų informaciją turinčių objektų.
    * Pagal šią informaciją nusprendžiame, prie kurių IP adresų ir po kiek paketų pridėti*/
    private StorageInTimeDomain increasePacketCounts(List<PacketsInfo> packetCountsToIncrease) {
        for (PacketsInfo pc: packetCountsToIncrease) {
            String ip = getIp(pc);

            PacketsInfo countByDestination = packetCountsGroupedByIp.get(ip);
            /* Jeigu dar nėra tokio IP adreso, tai sukuriame, pradinis skaičius 0, visa kita informacija
            šiaip sau */
            if (countByDestination == null) {
                countByDestination = new PacketsInfo(null, ip, pc.getTime(), 0, 0);
                packetCountsGroupedByIp.put(ip, countByDestination);
            }
            countByDestination.setCount(countByDestination.getCount() + pc.getCount());
        }
        return this;
    }

    public StorageInTimeDomain addCurrentIntervalToStorage() {
        if (currentTimePacketCounts.isEmpty()) {
            return this;
        }

        this.increasePacketCounts(currentTimePacketCounts);

        listOfListOfPacketCounts.add(currentTimePacketCounts);
        if (listOfListOfPacketCounts.size() > windowWidth) {
            this.decreasePacketCounts(listOfListOfPacketCounts.remove(0));
        }

        addNewFrom(currentTimePacketCounts.get(0).getFrom());
        countEntropy();
        addCurrentEntropyToEntropyList();

        return this;
    }

    public StorageInTimeDomain cleanCurrentInterval() {
        this.currentTimePacketCounts = new ArrayList<PacketsInfo>();
        this.notNormalizedCurrentTimeEntropy = 0D;
        this.normalizedCurrentTimeEntropy = 0D;
        return this;
    }

    public boolean timeExceedsCurrentTime(Timestamp time) {
        return time.getTime() > this.currentTimeInMilis;
    }

    private StorageInTimeDomain updateCurrentTime(Timestamp time) {
        this.currentTimeInMilis = time.getTime();
        return this;
    }

    private void addNewFrom(Long from) {
        this.from += from;

        this.listOfFrom.add(from);
        if (this.listOfFrom.size() > this.windowWidth) {
            this.from -= this.listOfFrom.remove(0);
        }
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public void addNewPacketInfo(PacketsInfo packetCount) {
        currentTimePacketCounts.add(packetCount);
        updateCurrentTime(packetCount.getTime());
    }

    public List<ValueInTimeInterval> getListOfEntropies() {
        return listOfEntropies;
    }

    private void countEntropy() {
        for (String ip: packetCountsGroupedByIp.keySet()) {
            Long count = packetCountsGroupedByIp.get(ip).getCount();

            Double probability = ((double) count) / from;
            notNormalizedCurrentTimeEntropy -= probability * (Math.log(probability) / Math.log(2));
        }
        Double maximumEntropy = -Math.log(1D / from) / Math.log(2);
        normalizedCurrentTimeEntropy = notNormalizedCurrentTimeEntropy / maximumEntropy;
    }

    private void addCurrentEntropyToEntropyList() {
        ValueInTimeInterval e = new ValueInTimeInterval();
        e.setTime(new Timestamp(currentTimeInMilis));
        e.setValue(normalizedCurrentTimeEntropy);

        listOfEntropies.add(e);

        if (e.getValue() < minimumEntropy.getValue()) {
            minimumEntropy = e;
        }

        if (normalizedCurrentTimeEntropy < addToAttackListIfLowerThan) {
            attackList.add(e);
        }
    }

    public ValueInTimeInterval getMinimumEntropy() {
        return minimumEntropy;
    }

    public void setMinimumEntropy(ValueInTimeInterval minimumEntropy) {
        this.minimumEntropy = minimumEntropy;
    }


    public void setAddToAttackListIfLowerThan(Double addToAttackListIfLowerThan) {
        this.addToAttackListIfLowerThan = addToAttackListIfLowerThan;
    }

    public List<ValueInTimeInterval> getAttackList() {
        return attackList;
    }
}
