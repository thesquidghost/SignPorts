# SignPorts

SignPorts is a Bukkit/Spigot plugin that allows players to create and use sign-based teleportation ports within their claims. This plugin integrates with GriefDefender for claim management and PlaceholderAPI for enhanced customization.

## Currently Known Issues and Projects:

1. Signports retain item metadata after setting an item as the port's menu icon.

## Features

- UPDATE! ADDED CROUCH + RIGHT CLICK MENU FOR LOCKING SIGNS
- Create teleportation points using signs within player-owned claims
- GUI-based menu for easy navigation and management of SignPorts
- Customizable sign format and messages
- Integration with GriefDefender for claim-based permissions
- Support for PlaceholderAPI for dynamic text replacement

## Installation

1. Ensure you have GriefDefender and PlaceholderAPI installed on your server.
2. Download the SignPorts.jar file from the releases page.
3. Place the SignPorts.jar file in your server's `plugins` folder.
4. Restart your server or run the `/reload confirm` command.

## Commands

- `/signport create <name>` - Create a new SignPort
- `/signport list` - List all available SignPorts
- `/signport remove <name>` - Remove a SignPort
- `/signport teleport <name>` - Teleport to a SignPort
- `/signport gui` - Open the SignPort GUI
- `/signportmenu` - Open the SignPort menu
- `/confirm` - Confirm a SignPort setup action
- `/setname <name>` - Set the name for a SignPort during setup
- `/setdesc <description>` - Set the description for a SignPort during setup

## Permissions

- `signports.create` - Allows creation of SignPorts
- `signports.use` - Allows usage of SignPorts
- `signports.list` - Allows listing of SignPorts
- `signports.remove` - Allows removal of SignPorts
- `signports.teleport` - Allows teleportation to SignPorts
- `signports.gui` - Allows using the SignPort GUI
- `signports.admin` - Gives access to all SignPorts commands and features

## Configuration

The plugin's configuration can be found in the `config.yml` file in the SignPorts plugin folder. Here you can customize various aspects of the plugin, such as the SignPort identifier and messages.

## Usage

1. Place a sign in your claim.
2. Write "[SignPort]" (or your configured identifier) on the first line of the sign.
3. Right-click the sign to start the setup process.
4. Follow the instructions to complete the SignPort setup.
5. Use the SignPort GUI or commands to manage and use your SignPorts.

## Support

If you encounter any issues or have questions, please open an issue on our GitHub repository or contact us through our support channels.

## Contributing

We welcome contributions to the SignPorts plugin. Please feel free to submit pull requests or open issues for any bugs or feature requests.

## License

This project is licensed under the [MIT License](LICENSE).
