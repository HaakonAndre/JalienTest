package alien.shell.commands;

import java.util.List;

import alien.catalogue.FileSystemUtils;
import alien.catalogue.LFN_CSD;
import alien.user.UsersHelper;

/**
 * @author mmmartin
 * @since November 27, 2018
 */
public class JAliEnCommandcd_csd extends JAliEnBaseCommand {

	@Override
	public void run() {

		LFN_CSD newDir = null;

		if (alArguments != null && alArguments.size() > 0)
			newDir = commander.c_api.getLFNCSD(FileSystemUtils.getAbsolutePath(commander.user.getName(), commander.getCurrentDirName(), alArguments.get(0)));
		else
			newDir = commander.c_api.getLFNCSD(UsersHelper.getHomeDir(commander.user.getName()));

		if (newDir != null && newDir.exists) {
			if (newDir.isDirectory()) {
				commander.curDirCsd = newDir;
			}
			else
				commander.setReturnCode(1, "Cannot cd: " + alArguments.get(0) + " is file, not a directory");
		}
		else
			commander.setReturnCode(1, "No such file or directory");
	}

	/**
	 * printout the help info, none for this command
	 */
	@Override
	public void printHelp() {
		// ignore
	}

	/**
	 * cd can run without arguments
	 *
	 * @return <code>true</code>
	 */
	@Override
	public boolean canRunWithoutArguments() {
		return true;
	}

	/**
	 * Constructor needed for the command factory in commander
	 *
	 * @param commander
	 *
	 * @param alArguments
	 *            the arguments of the command
	 */
	public JAliEnCommandcd_csd(final JAliEnCOMMander commander, final List<String> alArguments) {
		super(commander, alArguments);
	}
}
