# Global Type Validation

- A message sender must be different from the receiver;
- Branching and recursion (goto) are terminal operations;
- In a branch, when a role is not the choice subject, it becomes "enabled" upon receiving a message;
- In a choice, the choice subject starts as enabled;
- In a choice, a role must always be enabled by the same role (consistent external choice);
- In a choice, a role must be enabled exactly zero times or in every branch (no unfinished roles);
- In a choice branch, if a disabled role sends a message, is the subject of a choice,
  or if the branch ends in recursion, its behaviour (local type) must be the same for all branches;
