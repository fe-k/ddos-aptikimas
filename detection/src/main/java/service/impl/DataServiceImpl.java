package service.impl;


import dao.PacketDao;
import entities.Packet;
import exceptions.GeneralException;
import org.springframework.beans.factory.annotation.Autowired;
import service.DataService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataServiceImpl implements DataService {

    @Autowired
    private PacketDao packetDao;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void uploadFileToDatabase() throws GeneralException {
        String[] files = {
                "C:\\Users\\K\\Desktop\\TrainingData\\w1\\1d-inside.csv"
                , "C:\\Users\\K\\Desktop\\TrainingData\\w1\\1d-outside.csv"
        };

        for (int i = 0; i < files.length; i++) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(files[i]));
                String line;
                while ((line = br.readLine()) != null) {
                    Packet packet = getPacket(line, i);
                    packetDao.save(packet);
                }
            } catch (Exception e) {
                throw new GeneralException("Could not upload file to the database!", e);
            }
        }
    }

    private Packet getPacket(String line, int fileIndex) throws GeneralException {
        String[] cols = line.split("\\\",\\\"");
        cols[0] = cols[0].substring(1);
        cols[cols.length - 1] = cols[cols.length - 1].substring(0, cols[cols.length - 1].length() - 1);

        Packet packet = new Packet();
        packet.setNumber(Integer.valueOf(cols[0]));

        try {
            String time = cols[1];
            String[] parts = time.split("\\.");
            Date parsedDate = simpleDateFormat.parse(parts[0]);
            Timestamp timestamp = new Timestamp(parsedDate.getTime());
            timestamp.setNanos(Integer.parseInt(parts[1]) * 1000);
            packet.setTimestamp(timestamp);
        } catch (Exception e) {
            throw new GeneralException("Could", e);
        }


        packet.setSource(cols[2]);
        packet.setDestination(cols[3]);
        packet.setProtocol(cols[4]);
        packet.setLength(Integer.valueOf(cols[5]));
        packet.setInfo(cols[6]);
        packet.setFileName(String.valueOf(fileIndex));

        return packet;
    }

}
