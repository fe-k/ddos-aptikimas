package dto;

public class StorageByDestinationInTimeDomain extends StorageInTimeDomain {

    @Override
    String getIp(PacketsInfo pc) {
        return pc.getDestination();
    }
}
