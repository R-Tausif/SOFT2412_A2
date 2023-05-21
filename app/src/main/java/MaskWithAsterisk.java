import java.io.*;

// mask password with asterisk java console
class ThreadDisappear implements Runnable {
    private boolean end;
    public ThreadDisappear(String prompt) {
        System.out.print(prompt);
    }
    public void run() {
        end = true;
        while (end) {
            System.out.print("\010*");
            try {
                Thread.currentThread().sleep(1);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    public void maskEnd() {
        this.end = false;

    }

}


class MaskWithAsterisk {
    public String readPassword(String input) {
        String password = "";
        ThreadDisappear td = new ThreadDisappear(input);
        Thread t = new Thread(td);
        t.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            password = br.readLine();
            td.maskEnd();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return password;
    }
}

/*
References:
http://www.cse.chalmers.se/edu/year/2015/course/TDA602/Eraserlab/pwdmasking.html
https://www.golinuxcloud.com/mask-password-with-asterisk-java-console/


 */