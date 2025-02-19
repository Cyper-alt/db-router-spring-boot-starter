package cn.bugstack.middleware.db.router;

/**
 * @author Chen
 * @description
 * @createTime 2024/12/18 20:18
 */
public class DBRouterBase {

    private String tbIdx;

    public String getTbIdx() {
        return DBContextHolder.getTBKey();
    }

}
