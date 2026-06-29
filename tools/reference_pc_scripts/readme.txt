HOW TO INSTALL

EXTRACT THE STUFF FROM THE ZIP FILE TO \Steam\steamapps\common\GarrysMod AND THEN RUN

GO BACK TO \Steam\steamapps\common\GarrysMod AND LAUNCH THE SHORTCUT AND THEN RUN "mod_vpk" to make vpk files work and then press mod launch



for renaming the gmodsdk_fix folder rename it to whatever you want
then mod_launch change it to the renamed folder from this "gmod -game gmodsdk_fix" to "gmod -game (rennamed folder)"
after that go to mod_vpk and change this line from (set "MOD_PATH=%~dp0gmodsdk_fix") to [set "MOD_PATH=%~dp0(rennamed folder name)"]


for releasing the mod, launch "mod_vpk_delete" to avoid legal trouble. For making an zip file, only grab mod_vpk, mod_launch and the renamed folder