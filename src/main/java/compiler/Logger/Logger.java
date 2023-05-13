package compiler.Logger;

public class Logger {

    private static Logger _instance = null;

    enum Level {
        NONE,
        INFO,
        WARNING,
        ERROR
    }

    private Logger(){}

    public static synchronized Logger getInstance(){
        if(_instance == null){
            return _instance = new Logger();
        }
        return _instance;
    }

    public void log(String message, Level l){
        // TODO do better than this
        //if(l == null) l = Level.NONE;
        //if(l == Level.NONE) return;
        System.out.println(message);
    }
}
