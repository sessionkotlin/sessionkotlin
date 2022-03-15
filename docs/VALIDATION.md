# Global Type Validation

## Choice

- In a choice branch, a role can be enabled or disabled.
- The choice target is the only enabled role at the start.
- Only enabled roles can initiate an interaction.
- Roles that receive messages become enabled.
- For every role that is not the choice target, the peer of the enabling action must be the same in all cases.
- A role that is enabled in every case becomes enabled when the choice concludes.
- A role can either be enabled in every case or no case at all (no unfinished roles).
 
## Recursion

- All roles must be enabled when calling rec().
- A rec() call cannot be followed by any instruction.
