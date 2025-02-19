package cn.bugstack.middleware.db.router.util;

public class HashUtils {

    /**
     * FNV1_32_HASH
     *
     * @param obj
     *         object
     * @return hashcode
     */
    public static int hashcode(Object obj) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        String str = obj.toString();
        for (int i = 0; i < str.length(); i++) {
            hash = (hash ^ str.charAt(i)) * p;
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        if (hash < 0) {
            hash = Math.abs(hash);
        }
        //System.out.println("hash computer:" + hash);
        return hash;
    }
}
