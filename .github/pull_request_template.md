<!--
Thank you for contributing to Neu21Plus!

Please give your PR a descriptive title following these conventions:
  Add <feature description>       — New features
  Fix <bug description>           — Bug fixes
  Remove <what and why>           — Removing deprecated code
  Port <original NEU feature>     — Features ported from original NEU
  Improve <what and how>          — Enhancements to existing features
  Refactor <component>            — Code refactoring without behavior changes
  Update <dependency/config>      — Dependency or config updates
  meta: <description>             — Changes that don't affect end-users

Do NOT end your PR title with a period (.).

If your PR bundles multiple features, consider opening separate PRs.
-->

## Description
<!-- Provide a clear description of what this PR does and why. -->


## Type of Change
<!-- Check all that apply -->
- [ ] 🆕 New feature
- [ ] 🐛 Bug fix
- [ ] 🔄 Port from original NEU
- [ ] ♻️ Refactor / Code cleanup
- [ ] ⚡ Performance improvement
- [ ] 📝 Documentation update
- [ ] 🔧 Build / CI change
- [ ] 💥 Breaking change

## Related Issues
<!-- Link any related issues: Fixes #123, Closes #456 -->
<!-- Delete this section if none -->


## How Has This Been Tested?
<!-- Describe how you tested this change -->
- [ ] Compiles without errors (`./gradlew build`)
- [ ] Tested in-game on MC 26.1 Fabric
- [ ] No new warnings in build output
- [ ] Existing features still work correctly

### Test Environment
- Minecraft Version: 26.1
- Fabric Loader: 0.19.2
- Java: 25


## Checklist
<!-- Go through each item before requesting review -->
- [ ] My code follows the project's coding style (no @Suppress, no comments, no warnings)
- [ ] I have verified this uses MC 26.1 APIs (not deprecated 1.8.9 APIs)
- [ ] Data Components used instead of NBT where applicable
- [ ] Mojang official mappings used (no SRG/MCP)
- [ ] My changes generate no new compiler warnings
- [ ] Config options added to NeuConfig if needed
- [ ] I have tested this change and it works as expected


## Screenshots / Recordings
<!-- If applicable, add screenshots or recordings of the change -->
<!-- Delete this section if not applicable -->


## Additional Notes
<!-- Any additional context, edge cases, or things reviewers should know -->
<!-- Delete this section if not applicable -->
