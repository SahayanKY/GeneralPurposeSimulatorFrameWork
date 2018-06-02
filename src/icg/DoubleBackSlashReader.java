package icg;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

public class DoubleBackSlashReader extends FilterReader{
	private boolean inInBackslash = false;

	DoubleBackSlashReader(Reader reader){
		super(reader);
	}

	@Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (len <= 0) return 0;
        int result = 0;
        synchronized (lock) {
            for (int i = 0; i < len; ++i) {
                int c = read1();
                if (c < 0) break;
                cbuf[off + i] = (char)c;
                ++result;
            }
        }
        return result == 0 ? -1 : result;
    }

	private int read1() throws IOException {
        if (inInBackslash) {
            inInBackslash = false;
            return '\\';
        }
        int result = super.read();
        if (result == '\\') inInBackslash = true;
        return result;
    }
}
