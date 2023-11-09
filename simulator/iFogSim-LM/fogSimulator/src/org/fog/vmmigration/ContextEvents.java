package org.fog.vmmigration;

public class ContextEvents {
    public static final int CHECK_NEW_STEP = 50011;
    public static final int MAKE_DECISION_MIGRATION = 70000;
    public static final int START_MIGRATION_UP = 5002;
    public static final int DELIVERY_VM = 5003;
    public static final int MIGRATE_VM = 5004;
    public static final int CHECK_SHOLD_MIGRATE = 5006;
    public static final int APP_SUBMIT_MIGRATE = 5007;
    public static final int START_MIGRATION_DOWN = 5008;

    /*Mudar lugar dessas constantes*/
    /*
    Time para a próxima verificação após a última
    * */
    public static final int TIME_TO_MAKE_DECISION_MIGRATION = 60;
}
