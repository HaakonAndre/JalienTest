package alien.shell.commands;

import java.util.ArrayList;

import joptsimple.OptionException;
import alien.catalogue.FileSystemUtils;
import alien.catalogue.LFN;
import alien.user.AuthorizationChecker;

/**
 * @author ron
 * @since Oct 27, 2011
 */
public class JAliEnCommandrmdir extends JAliEnBaseCommand {

	@Override
	public void run() {

		for (String path : alArguments) {

			LFN dir = commander.c_api.getLFN(FileSystemUtils
					.getAbsolutePath(commander.user
							.getName(), commander
							.getCurrentDir()
							.getCanonicalName(), path));

			if (dir!=null && dir.exists) {
				if (dir.isDirectory()) {
					if (AuthorizationChecker.canWrite(dir, commander.user)) {

						if (!commander.c_api
								.removeCatalogueDirectory(dir.getCanonicalName())) {
							out.printErrln("Could not remove directory: "
									+ path);
							out.printErrln("Sorry, this command is not implemented yet.");
						}

					} else {
						if (!isSilent())
							out.printErrln("Permission denied on directory: ["
									+ path + "]");
					}

				} else {
					if (!isSilent())
						out.printErrln("Not a directory: [" + path + "]");
				}
			} else {
				if (!isSilent())
					out.printErrln("No such file or directory: [" + path + "]");
			}
		}
	}

	/**
	 * printout the help info
	 */
	@Override
	public void printHelp() {

		out.printOutln();
		out.printOutln(helpUsage("rmdir",
				" <directory> [<directory>[,<directory>]]"));
		out.printOutln(helpStartOptions());
		out.printOutln(helpOption("-silent","execute command silently"));
		out.printOutln();
	}

	/**
	 * mkdir cannot run without arguments
	 * 
	 * @return <code>false</code>
	 */
	@Override
	public boolean canRunWithoutArguments() {
		return false;
	}

	/**
	 * Constructor needed for the command factory in commander
	 * 
	 * @param commander
	 * @param out
	 * 
	 * @param alArguments
	 *            the arguments of the command
	 * @throws OptionException
	 */
	public JAliEnCommandrmdir(JAliEnCOMMander commander, UIPrintWriter out,
			final ArrayList<String> alArguments) throws OptionException {
		super(commander, out, alArguments);

	}
}
