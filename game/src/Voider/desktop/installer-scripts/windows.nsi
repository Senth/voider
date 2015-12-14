# ----------------------------
# Variables changed by Gradle
# ----------------------------
Var VOIDER_FOLDER
Var PROGRAM_FILES_USE # 32/64 bit

StrCpy $VOIDER_BUILD "Voider"
StrCpy $PROGRAM_FILES_USE $PROGRAMFILES64


Name $VOIDER_BUILD

# Name of the installer
Outfile "Voider Setup.exe"

# Set default installation directory
InstallDir $PROGRAM_FILES_USE

# For removing Start Menu shortcut in Windows 7
RequestExecutionLevel user

# Default section
Section
	# Define the output path fo this file
	SetOutPath $INSTDIR

	File $VOIDER_BUILD

	WriteUninstaller $INSTDIR\$VOIDER_BUILD\uninstaller.exe

	# Create a shortcut named Voider in the start menu programs
	CreateDirectory $SMPROGRAMS\$VOIDER_BUILD
	CreateShortCut "$SMPROGRAMS\$VOIDER_BUILD\Uninstall.lnk" "$INSTDIR\$VOIDER_BUILD\uninstaller.exe"
	CreateShortCut "$SMPROGRAMS\$VOIDER_BUILD\$VOIDER_BUILD.lnk" "$INSTDIR\$VOIDER_BUILD\$VOIDER_BUILD.exe"
SectionEnd


# Create a section to define what the uninstaller does
Section "Uninstall"
	
# Always delete uninstaller first
Delete $INSTDIR\$VOIDER_BUILD\uninstaller.exe
Delete $INSTDIR\$VOIDER_BUILD

# Shortcuts
Delete "$SMPROGRAMS\$VOIDER_BUILD\Uninstall.lnk"
Delete "$SMPROGRAMS\$VOIDER_BUILD\$VOIDER_BUILD.lnk"

# User content etc
Delete $PROFILE\$VOIDER_BUILD
Delete $PROFILE\.prefs\$VOIDER_BUILD*

SectionEnd
