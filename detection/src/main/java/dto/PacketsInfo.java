package dto;

import java.sql.Timestamp;

public class PacketsInfo {
    private String source;      //šaltinio IP adresas
    private String destination; //paskirties IP adresas

    //Vienu metu naudojamas tik vienas iš intervalų
    private Timestamp time;             //laikinis intervalas
    private long numberInCountDomain;   // paketų skaičiaus intervalas

    private long count;         //pasako, kiek radom paketų
    private long from;          //pasako, kiek paketų buvo iš viso

    public PacketsInfo(String source, String destination, Timestamp time, long count, long from) {
        this.source = source;
        this.destination = destination;
        this.time = time;
        this.count = count;
        this.from = from;
    }

    public PacketsInfo(String source, String destination, long numberInCountDomain, long count, long from) {
        this.source = source;
        this.destination = destination;
        this.numberInCountDomain = numberInCountDomain;
        this.count = count;
        this.from = from;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public long getNumberInCountDomain() {
        return numberInCountDomain;
    }

    public void setNumberInCountDomain(long numberInCountDomain) {
        this.numberInCountDomain = numberInCountDomain;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getFrom() {
        return from;
    }

    public void setFrom(long from) {
        this.from = from;
    }
}
