# Global Type Validation

- A message sender must be different from the receiver;
- Branching and recursion (goto) are terminal operations;
- In a branch, when a role is not the choice subject, it becomes "enabled" upon receiving a message;
- In a choice, the choice subject starts as enabled;
- In a choice, a role must always be enabled by the same role (consistent external choice);
- In a choice, a role must be enabled exactly zero times or in every branch (no unfinished roles);
- In a choice, if a disabled role sends a message or is the subject of a choice in some branch, if its behaviour (local
  type) must be the same for all branches;
- In a choice, if a branch has a recursive call and a role is disabled, that role's behaviour (local type) must be the
  same for all branches.
