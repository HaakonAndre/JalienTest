package alien.shell.commands;

import java.util.ArrayList;

import alien.perl.commands.AlienTime;

/**
 * @author ron
 * @since June 4, 2011
 */
public class JAliEnCommandtime extends JAliEnBaseCommand {


	@Override
	public void run() {

		if (alArguments.size() < 2) {
			printHelp();
			return;
		}

		ArrayList<String> args = new ArrayList<String>();
		args.addAll(alArguments);
		int times = 0;
		try {
			times = Integer.parseInt(alArguments.get(0));
			args.remove(alArguments.get(0));
		} catch (NumberFormatException e) {
			printHelp();
		}
		String command = alArguments.get(1);
		args.remove(alArguments.get(1));

		JAliEnBaseCommand comm = null;
		try {
			comm = JAliEnCOMMander
					.getCommand(command, new Object[] {  commander, out,args });
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		//comm.silent();

		ArrayList<Long> timings = new ArrayList<Long>(times);
		for (int t = 0; t < times; t++) {
			long lStart = System.currentTimeMillis();

			comm.run();
			timings.add(Long.valueOf(System.currentTimeMillis() - lStart));
		}
		long total = 0;
		for (Long t : timings)
			total += t.longValue();
		
		for(String s:args)
			command += " " + s;
		
		out.printOutln("\""+command+ "\" with #" + times+ " tries,\tavr/total msec:\t\t"+(total / times)+"/"+total);
	}

	/**
	 * printout the help info
	 */
	@Override
	public void printHelp() {

		out.printOutln(AlienTime.getStamp()
				+ "Usage: time <times>  <command> [command_arguments] ");
	}

	/**
	 * cat cannot run without arguments
	 * 
	 * @return <code>false</code>
	 */
	@Override
	public boolean canRunWithoutArguments() {
		return false;
	}

	/**
	 * Constructor needed for the command factory in commander
	 * @param commander 
	 * @param out 
	 * 
	 * @param alArguments
	 *            the arguments of the command
	 */
	public JAliEnCommandtime(JAliEnCOMMander commander, UIPrintWriter out, final ArrayList<String> alArguments){
		super(commander, out,alArguments);
	}
}
