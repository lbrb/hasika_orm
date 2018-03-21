package cn.migu.hasika.database.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.HashMap;

/**
 * Created by hasika on 2018/3/15.
 */

public class SerializableUtil {

    public static byte[] serialize(HashMap<Integer, Long> hashMap) {
        try {
            ByteArrayOutputStream mem_out = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(mem_out);

            out.writeObject(hashMap);

            out.close();
            mem_out.close();

            byte[] bytes = mem_out.toByteArray();
            return bytes;
        } catch (IOException e) {
            return null;
        }
    }

    public static HashMap<Integer, Long> deserialize(byte[] bytes) {
        try {
            ByteArrayInputStream mem_in = new ByteArrayInputStream(bytes);
            ObjectInputStream in = new ObjectInputStream(mem_in);

            HashMap<Integer, Long> hashMap = (HashMap<Integer, Long>) in.readObject();

            in.close();
            mem_in.close();

            return hashMap;
        } catch (StreamCorruptedException e) {
            return null;
        } catch (ClassNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

}
