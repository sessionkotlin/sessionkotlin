# Global Type Validation

- A message sender must be different from the receiver;
- Branching and recursion (goto) are terminal operations;
- In a branch, when a role is not the choice subject, it becomes "enabled" upon receiving a message;
- In a choice, the choice subject starts as enabled;
- In a choice, a role must always be enabled by the same role (consistent external choice);
- In a choice, a role must be enabled exactly zero times or in every branch (no unfinished roles);
- In a choice branch, if a disabled role sends a message, is the subject of a choice,
  or if the branch ends in recursion, its behaviour (local type) must be the same for all branches;
- If a recursion definitions is directly followed by the recursion point, then both constructs are
- The sender must know all the names used in the refinement condition, if present.
- All message labels must be unique.
- All branch labels must be unique.
- All conditions must be "parseable" for the projection to succeed.

# Projection optimizations

- Recursion definitions that have no corresponding recursion points are erased.
- In choice branches, if a role does not send or receive messages after a recursion definition it means that the role
  does not participate for the rest of the protocol. The recursion is (locally) erased.
- In a choice, if all branches are empty (i.e. LocalTypeEnd), the choice is (locally) erased.
