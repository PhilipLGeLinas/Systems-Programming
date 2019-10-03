import java.util.*;

class Shell extends Thread
{
    private int commandNumber;
    boolean running = true;

    // A constructor that generates a shell with a command number of zero.
    public Shell() { commandNumber = 0; }

    // Execute one command and remove list content.
    public void executeCommand(List<String> commandList, int count)
    {
        SysLib.cout(commandList.get(0) + "\n"); // Display the name of the class.
        String[] arr = (String[]) commandList.toArray(new String[count]); // Convert to a string array.
        if (SysLib.exec(arr) == -1)
        {
            SysLib.cerr(arr[0] + " failed to load.\n"); // Display error message.
        }
    }

    // The run method is called upon program execution.
    public void run()
    {
        while (running)
        {
            commandNumber++; // Increment the command number.
            String currentCommand = ""; // The command typed in by the user.

            // Read all user-typed commands.
            do {
                SysLib.cout("shell[" + commandNumber + "]% ");
                StringBuffer userInput = new StringBuffer();
                SysLib.cin(userInput);
                currentCommand = userInput.toString();
            } while (currentCommand.length() == 0);

            String[] arguments = SysLib.stringToArgs(currentCommand); // Stores all arguments.
            List<String> commandList = new ArrayList<String>(); // Stores separated commands.
            int count = 0;

            // Iterate over each value in the arguments array.
            for (int i = 0; i < arguments.length; i++)
            {
                // If the user types 'exit', quit running.
                if (arguments[i].equals("exit"))
                {
                    SysLib.exit(); // Exit without error.
                    running = false;
                    break;
                }
                // If we encounter an ampersand, execute commands simultaneously.
                if (arguments[i].equals("&") && !commandList.isEmpty())
                {
                    executeCommand(commandList, count);
                    commandList = new ArrayList<String>(); // Empty the command list.
                    count = 0;
                }
                // If we encounter a semicolon, execute commands sequentially.
                else if (arguments[i].equals(";") && !commandList.isEmpty())
                {
                    executeCommand(commandList, count);
                    SysLib.join(); // Wait for child thread to be terminated.
                    commandList = new ArrayList<String>(); // Empty the command list.
                    count = 0;
                } else {
                    commandList.add(arguments[i]);
                    count++;
                }
            }
            // If we encounter the last command, simply execute.
            if (!commandList.isEmpty())
            {
                executeCommand(commandList, count);
                SysLib.join(); // Wait for child thread to be terminated.
            }
        }
    }
}