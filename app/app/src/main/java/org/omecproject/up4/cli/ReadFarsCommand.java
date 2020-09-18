package org.omecproject.up4.cli;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.omecproject.up4.ForwardingActionRule;
import org.omecproject.up4.Up4Service;
import org.onosproject.cli.AbstractShellCommand;

/**
 * UP4 FAR read command.
 */
@Service
@Command(scope = "up4", name = "read-fars",
        description = "Print all FARS installed in the dataplane")
public class ReadFarsCommand extends AbstractShellCommand {

    @Override
    protected void doExecute() {
        Up4Service app = get(Up4Service.class);

        for (ForwardingActionRule far : app.getUpfProgrammable().getInstalledFars()) {
            print(far.toString());
        }
    }
}