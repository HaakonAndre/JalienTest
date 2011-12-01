package alien.log;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * @author Alina Grigoras
 * Sedding INFO to a specific file
 *
 */
public class InfoFileHandler extends FileHandler {

	/**creating a simple FileHandler on which we apply a level and a filter
	 * @throws IOException
	 * @throws SecurityException
	 */
	public InfoFileHandler() throws IOException, SecurityException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public synchronized void setLevel(Level newLevel) throws SecurityException {
		// TODO Auto-generated method stub
		super.setLevel(Level.INFO);
	}
	
	@Override
	public void setFilter(Filter newFilter) throws SecurityException {
		// TODO Auto-generated method stub
		super.setFilter(new Filter() {

			@Override
			public boolean isLoggable(LogRecord record) {
				// TODO Auto-generated method stub
				if(record.getLevel() != Level.INFO)
					return false;
				return true;
			}
		});
	}
}
