/*
 * Vulture - a server protection plugin designed for Minecraft 1.8.9 servers.
 * Copyright (C) 2024 Foulest (https://github.com/Foulest)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package net.foulest.vulture.check.type.clientbrand;

import com.github.retrooper.packetevents.event.CancellableEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import net.foulest.vulture.check.Check;
import net.foulest.vulture.check.CheckInfo;
import net.foulest.vulture.check.CheckType;
import net.foulest.vulture.check.type.clientbrand.type.DataType;
import net.foulest.vulture.check.type.clientbrand.type.PayloadType;
import net.foulest.vulture.data.PlayerData;
import net.foulest.vulture.util.FileUtil;
import net.foulest.vulture.util.KickUtil;
import net.foulest.vulture.util.MessageUtil;
import net.foulest.vulture.util.Settings;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@CheckInfo(name = "ClientBrand", type = CheckType.CLIENTBRAND, punishable = false,
        description = "Checks for modified client brands.")
public class ClientBrand extends Check {

    private static final List<PayloadType> BRANDS = Arrays.asList(
            new PayloadType("LiteLoader", "LiteLoader", DataType.BRAND, false),
            new PayloadType("\rFeather Forge", "Feather Client", DataType.BRAND, false),
            new PayloadType("\tfml,forge", "Forge", DataType.BRAND, false),
            new PayloadType("\u0005forge", "Forge", DataType.BRAND, false),
            new PayloadType("\u0005PLC18", "PvPLounge Client", DataType.BRAND, false),
            new PayloadType("\u0006fabric", "Fabric", DataType.BRAND, false),
            new PayloadType("\u0007LiteLoader", "LiteLoader", DataType.BRAND, false),
            new PayloadType("\u0007fabric", "Fabric", DataType.BRAND, false),
            new PayloadType("\u0007labymod", "LabyMod", DataType.BRAND, false),
            new PayloadType("\u0007fml,forge", "Forge", DataType.BRAND, false),
            new PayloadType("\tFML,Forge", "Forge", DataType.BRAND, false),
            new PayloadType("\u0007vanilla", "Vanilla", DataType.BRAND, false),
            new PayloadType("\u000EFeather Fabric", "Feather Client", DataType.BRAND, false),
            new PayloadType("fabric", "Fabric", DataType.BRAND, false),
            new PayloadType("fml,forge", "Forge", DataType.BRAND, false),
            new PayloadType("vanilla", "Vanilla", DataType.BRAND, false),
            new PayloadType("\nminebuilders8", "Minebuilders Client", DataType.BRAND, false),
            new PayloadType("\nminebuilders9", "Minebuilders Client", DataType.BRAND, false),
            new PayloadType("\u0007badlion", "Badlion Client", DataType.BRAND, false),
            new PayloadType("lunar:apollo", "Lunar Client", DataType.BRAND, false),
            new PayloadType("\u000Bemberclient", "Ember Client", DataType.BRAND, false),

            new PayloadType("\u0005eyser", "Geyser Spoof", DataType.BRAND, true), // TODO: Test with real Geyser
            new PayloadType("\boptifine", "OptiFine Spoof", DataType.BRAND, true),
            new PayloadType("Created By", "Vape Client", DataType.BRAND, true),
            new PayloadType("Geyser", "Geyser Spoof", DataType.BRAND, true),
            new PayloadType("PLC18", "PvPLounge Client Spoof", DataType.BRAND, true),
            new PayloadType("Synergy", "Synergy Client", DataType.BRAND, true),
            new PayloadType("Vanilla", "Vanilla Spoof", DataType.BRAND, true),
            new PayloadType("\nLunar-Client", "Lunar Client Spoof", DataType.BRAND, true),
            new PayloadType("\u0002CB", "CheatBreaker Spoof", DataType.BRAND, true),
            new PayloadType("\u0003FML", "Forge Spoof", DataType.BRAND, true),
            new PayloadType("\u0003LMC", "LabyMod Spoof", DataType.BRAND, true),
            new PayloadType("\u0007Created By ", "Vape Client", DataType.BRAND, true),
            new PayloadType("\u0007Synergy", "Synergy Client", DataType.BRAND, true),
            new PayloadType("\u0007Vanilla", "Vanilla Spoof", DataType.BRAND, true),
            new PayloadType("\u000Blunarclient", "Lunar Client Spoof", DataType.BRAND, true)
    );

    private static final List<PayloadType> REGISTER_DATA = Arrays.asList(
            new PayloadType("BungeeCord", "BungeeCord", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("legacy:redisbungee", "BungeeCord", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("FML", "Forge", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("FML|HS", "Forge", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("FML|MP", "Forge", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("FOR", "Forge", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("FORGE", "Forge", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("ForgeMicroblock", "Forge", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("ForgeMultipart", "Forge", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("fml:loginwrapper", "Forge", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("fml:play", "Forge", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("forge:split", "Forge", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("forge:split_11", "Forge", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("fml:handshake", "Forge", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("fabric-screen-handle", "Fabric", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("fabric:attachment_sy", "Fabric", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("lunar:apollo", "Lunar Client", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("lunarclient:pm", "Lunar Client", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("apollo:json", "Lunar Client", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("transfer:channel", "Lunar Client", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("labymod3:main", "LabyMod", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("labymod:neo", "LabyMod", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("minecraft:intave", "LabyMod", DataType.REGISTER_DATA_OTHER, false),

            new PayloadType("EB", "Unknown (EB)", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("ES", "Unknown (ES)", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("ET", "Unknown (ET)", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("GEN", "Unknown (GEN)", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("autoconfig", "Unknown (autoconfig)", DataType.REGISTER_DATA_OTHER, false),

            new PayloadType("AppleCore", "AppleCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("craftingtweaks", "CraftingTweaks", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("Emoticons", "Emoticons", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("emoticons", "Emoticons", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("hud", "Better HUD", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("essential|diffct", "Essential Mod", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BSM", "Better Sprinting", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("essential:", "Essential Mod", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("AE2", "AE2", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("Ascribe", "Ascribe", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("AS_BT", "ASMC", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("AS_IF", "ASMC", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("AS_MM", "ASMC", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("Animania", "Animania", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BETTERTABS", "Better Tabs", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BIN", "BinnieMods", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BOT", "Botania", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BQMSI", "Better Questing", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BQ_NET_CHAN", "Better Questing", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BQ_STANDARD", "Better Questing", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioAStand", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioAtlas", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioAtlasSWP", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioAtlasTGUI", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioAtlasWPT", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioClipboard", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioClock", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioDeskOpenGUI", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioDrillText", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioMCBEdit", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioMCBPage", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioMapPin", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioMeasure", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioOpenBook", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioPaintPress", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioPainting", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioPaintingC", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioPaneler", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioRBook", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioRBookLoad", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioRecipeCraft", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioRecipeText", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioRenderUpdate", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioSign", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioStockCompass", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioStockLog", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioStockTitle", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioType", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioTypeDelete", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioTypeFlag", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioTypeUpdate", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BiblioUpdateInv", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("CCL_INTERNAL", "CodeChicken Lib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("CallableHorses", "Callable Horses", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("CapsuleChannel", "Capsule", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("CarryOn", "Carry On", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ChickenChunks", "ChickenChunks", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ChiselsAndBits", "ChiselsAndBits", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("CoFH", "CoFHCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("CustomNPCs", "Custom NPCs", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("CustomNPCsPlayer", "Custom NPCs", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("EnderCore", "EnderCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("FLoco", "FunkyLocomotion", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("FlansMod", "Flan's Mod", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ForgottenItems", "Forgotten Items", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("GrCCellar", "GrowthCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("GuideAPI", "GuideAPI", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("HatStand", "HatStand", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("Hats", "Hats", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("InventoryTweaks", "InventoryTweaks", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("JEI", "JustEnoughItems", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("LB|CP", "Lost Books II", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("LogisticsPipes", "LogisticsPipes", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("MEK", "Mekanism", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("MFFS", "Modular ForceField System", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("Meson", "Meson", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("MineColonies", "MineColonies", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("MoCreatures", "Mo' Creatures", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("MorePlayerModels", "MorePlayerModels", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("Morph", "Morph", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("chunkpregenerator", "Chunk Pregenerator", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("carbonconfig", "Carbon Config", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("Mystcraft", "Mystcraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("NVLFF1710155810", "NVL's Force Fields", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("OBFUSCATE|HS", "Obfuscate", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("OBFUSCATE|PLAY", "Obfuscate", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("OCMC", "OpticCraft Client", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("OE_CHAN", "OreExcavation", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("OpenComputers", "OpenComputers", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("OpenMods|E", "OpenMods", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("OpenMods|M", "OpenMods", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("OpenMods|RPC", "OpenMods", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("OpenTerrainGenerator", "OpenTerrainGenerator", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("PERMISSIONSREPL", "WorldDownloader", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("PR|Integr", "ProjectRed", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("PR|Reloc", "ProjectRed", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("PR|Transp", "ProjectRed", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("PneumaticCraft", "PneumaticCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("PortalGun", "PortalGun", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("RC", "RebornCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("Replay|Restrict", "Replay Mod", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("SM|FX", "Special Mobs", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("SRParasites", "Scape and Run Parasites", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("Schematica", "Schematica", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("Sonar-Packets", "SonarCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("Structurize", "Structurize", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("TeamWizardry", "LibrarianLib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("TestDummy", "TestDummy", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("TschippLib", "TschippLib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("WDL|INIT", "WorldDownloader", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("WECUI", "WorldEdit CUI", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("WRCBE", "WRCBE", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("WorldControl", "WorldControl", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("XU2", "Extra Utilities 2", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("XercaChannel", "Xerca's Mods", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("abnormals_core:net", "Abnormal's Core", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("additionalpipes", "Additional Pipes", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("adhooks", "Advanced Hook Launchers", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("advancedcapes", "AdvancedCapes", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("advancedperipherals:", "Advanced Peripherals", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ae2ao:test", "AE2 Additional Opportunity", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("aether_legacy", "Aether Legacy", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("akashictome:main", "Akashic Tome", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("alexsmobs:main_chann", "Alex's Mobs", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ancientbeasts", "Beast Slayer", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("antiqueatlas", "Antique Atlas", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("anvilpatch", "Anvil Patch", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("aoa3:aoa_packets", "Advent of Ascension", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("apexcore:main", "ApexCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("apotheosis:apotheosi", "Apotheosis", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("appleskin", "AppleSkin", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("appleskin:sync", "AppleSkin", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("appliedenergistics2:", "Applied Energistics 2", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("architecturecraft", "Architecture Craft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("architectury:network", "Architectury", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("architectury:spawn_e", "Architectury", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("aroma1997core", "Aroma1997Core", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("aroma1997sdimension", "Aroma1997s Dimensional World", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("artemislib", "ArtemisLib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("artifacts", "Artifacts", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("asmodeuscore", "AsmodeusCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("astikorcarts", "Astikor Carts", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("astralsorcery:net_ch", "Astral Sorcery", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("atum", "Atum", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("atum:atum_channel", "Atum", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("autoreglib", "AutoRegLib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("autoreglib:main", "AutoRegLib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("backpack", "Backpacks", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("balancedflight:main_", "Balanced Flight", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("base", "Base", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("baubles", "Baubles", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("bbwands", "Better Builder's Wands", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("bdew.ae2stuff", "Bdew", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("bdew.compacter", "Bdew", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("bdew.multiblock", "Bdew", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("beneath", "The Beneath", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("betteranimalsplus", "BetterAnimalsPlus", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("bettercombatmod", "RLCombat", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("bettersurvival", "Better Survival", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("bibliocraft", "BiblioCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("bigreactors", "BigReactors", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("bigreactors:network", "Big Reactors", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("biomecolorizer:a", "BiomeColorizer", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("biomesoplenty", "Biomes O' Plenty", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("bloodarsenal", "Blood Arsenal", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("bloodmagic", "Blood Magic", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("bloodmoon", "Blood Moon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("boss_tools:boss_tool", "Boss Tools", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("boss_tools:key_bindi", "Boss Tools", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("botania", "Botania", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("bountiful:main", "Bountiful", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("bountifulbaubles", "Bountiful Baubles", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("brandonscore:network", "Brandon's Core", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("buildcraftbuilders", "BuildCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("buildcraftcore", "BuildCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("buildcraftlib", "BuildCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("buildcraftrobotics", "BuildCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("buildcrafttransport", "BuildCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("caelus:main", "Caelus", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("carryon:carryonpacke", "CarryOn", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ccl:internal", "CodeChicken Lib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cfm", "FurnitureMod", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cfm:network", "FurnitureMod", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("champions", "Champions", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("chancecubes:packets", "Chance Cubes", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("chisel", "Chisel", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("chisel:main", "Chisel", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("chococraft", "Chococraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("chococraftplus", "ChocoCraft Plus", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("chrs:barrels", "Charset", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("chrs:lib", "Charset", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("chrs:pocket", "Charset", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("chunkloaders:main", "ChunkLoaders", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("citadel:main_channel", "Citadel", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:ability_sy", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:ability_up", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:add_evolut", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:aspects_up", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:battle_app", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:battle_cap", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:battle_cha", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:battle_end", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:battle_fai", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:battle_hea", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:battle_ini", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:battle_mad", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:battle_mak", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:battle_mes", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:battle_mus", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:battle_per", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:battle_que", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:battle_rep", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:battle_set", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:battle_swa", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:battle_swi", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:battle_tra", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:battle_upd", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:benched_mo", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:berry_sync", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:caught_bal", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:clear_evol", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:close_npc_", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:close_past", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:close_pc", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:dex_entry_", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:dialogue_c", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:dialogue_o", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:ev_update", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:experience", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:fishing_ba", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:fossils", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:friendship", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:gender_upd", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:global_spe", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:health_upd", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:held_item_", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:initialize", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:interact_p", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:iv_update", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:move_clien", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:moves_sync", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:moveset_up", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:natural_ma", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:nature_upd", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:nickname_u", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:npcs_sync", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:open_move_", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:open_npc_e", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:open_party", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:open_pastu", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:open_pc", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:open_start", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:original_t", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:packets/fo", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:pasture_po", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:play_posab", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:player_int", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:pokedex_sy", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:pokedex_ui", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:pokerod_sy", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:properties", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:remove_cli", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:remove_evo", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:run_posabl", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:script_reg", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:server_con", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:server_set", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:set_client", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:set_party_", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:set_pc_box", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:set_pc_pok", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:shiny_upda", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:spawn_empt", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:spawn_gene", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:spawn_npc_", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:spawn_poke", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:spawn_snow", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:species_fe", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:species_sy", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:species_up", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:standard_s", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:state_upda", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:status_upd", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:summary_ui", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:swap_clien", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:team_join_", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:team_membe", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:team_reque", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:tethering_", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:toast", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:trade_acce", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:trade_canc", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:trade_comp", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:trade_offe", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:trade_proc", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:trade_star", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:trade_upda", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:tradeable_", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:unlock_rel", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cobblemon:unvalidate", "Cobblemon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cofh_core:general", "CoFH Core", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("collisiondamage", "Collision Damage", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("colytra", "Colytra", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("comforts:main", "Comforts", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("computercraft", "ComputerCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("computercraft:networ", "ComputerCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("computronics", "Computronics", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("configured:play", "Configured", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("connectiblechains:ma", "Connectible Chains", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("constructionwand:mai", "Construction Wand", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("contenttweaker", "Content Tweaker", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cookingforblockheads", "CookingForBlockheads", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("corpsecomplex", "Corpse Complex", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cosmeticarmorreworke", "Cosmetic Armor Reworked", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cqrepoured", "Chocolate Quest Repoured", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("craftingtableiv", "CraftingTable IV", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("craftstudioapi", "CraftStudioAPI", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("crafttweaker", "CraftTweaker", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("crafttweaker:main", "CraftTweaker", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("create:network", "Create", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("creativecore:0s", "CreativeCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("creativecore:1s", "CreativeCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("creativecore:2s", "CreativeCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("creativecore:3s", "CreativeCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("creativecore:4s", "CreativeCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("creativecore:5s", "CreativeCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("creativecore:6s", "CreativeCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("creativecore:7s", "CreativeCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("creativecore:main", "CreativeCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("creativemd", "CreativeMD", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("crossbow", "Crossbow", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cucumber", "Cucumber", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("culinaryconstruct", "CulinaryConstruct", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("curios:main", "Curios", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("customnpcs:packets", "Custom NPCs", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cyclic:main_channel", "Cyclic", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("damagetilt", "DamageTilt", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("darkutils:main", "Dark Utils", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("divinerpg:divinerpg_", "Divine RPG", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("draconicevolution:ne", "Draconic Evolution", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("dsurround", "Dynamic Surroundings", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("dummmmmmy:dummychann", "Dummmmmmy", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("easy_villagers:defau", "Easy Villagers", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("eidolon:network", "Eidolon", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("eiococ", "EnderIO", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("eleccore", "ElecCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("eleccoreloader", "ElecCoreloader", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("elenaidodge", "Elenai Dodge", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("elevatorid", "Elevator Mod", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("elevatorid:main_chan", "Elevator Mod", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("embers", "Embers Rekindled", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("endergetic:net", "Endergetic", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("enderio", "EnderIO", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("enderioconduits", "EnderIO", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("enderioinvpanel", "EnderIO", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("enderiomachines", "EnderIO", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("enderiopowertools", "EnderIO", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("enderstorage:network", "EnderStorage", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("endertanks:main_chan", "Ender Tanks", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("energycontrol", "Energy Control", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("enhancedcelestials:n", "Enhanced Celestials", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("entitypurger", "Entity Purger", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("eplus", "EnchantingPlus", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("exchangers", "Exchangers", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("extrabitmanipulation", "Extra Bit Manipulation", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("extracells", "ExtraCells", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("extraplanets", "ExtraPlanets", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("eyesinthedarkness", "Eyes in the Darkness", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("fabric:container/ope", "Fabric", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("fabric:registry/sync", "Fabric", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("factorymanager", "FactoryManager", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("fairylights", "FairyLights", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("fancymenu:execute_co", "FancyMenu", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("fantasticlib", "Fantastic Lib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("fantasyfurniture:net", "Fantasy's Furniture", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("farmingforblockheads", "Farming for Blockheads", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("fastbench", "FastWorkbench", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("fastbench:channel", "FastWorkbench", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("feather:client", "Feather Client", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("firstaid", "First Aid", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("fishingmadebetter", "Fishing Made Better", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("fluidlogged_api", "Fluidlogged API", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("fluxnetworks:main_ne", "Flux Networks", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("fluxnetworks", "Flux Networks", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("flywheel:network", "FlyWheel", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("forgeendertech", "Forge Endertech", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("forgemultipartcbe", "Forge", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ftbbackups:main", "FTB Backups", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ftblib", "FTB Lib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ftblib_edit_config", "FTB Lib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ftblib_my_team", "FTB Lib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ftbquests", "FTB Quests", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ftbquests_edit", "FTB Quests", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ftbutilities", "FTB Utilities", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ftbutilities_claims", "FTB Utilities", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ftbutilities_files", "FTB Utilities", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ftbutilities_stats", "FTB Utilities", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("futuremc", "FutureMC", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("galacticraft", "GalactiCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("galacticraftcore", "GalactiCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("gamify:gamify", "Gamify", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("gamifyslayer:gamifys", "Gamify Slayer", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("gasconduits", "GasConduits", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("geckolib3:main", "GeckoLib 3", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("goblinsanddungeons:m", "Goblins and Dungeons", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("grapplemodchannel", "Grapple Mod", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("gravestone:default", "Gravestone", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("gravisuit", "Gravisuit Classic", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("guideapi-vp:main", "GuideAPI", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("hammercore2", "Hammer Core", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("harvestcraft", "Pam's HarvestCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("hats:channel", "Hats", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("hexxitgear", "Hexxit Gear", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("hexxitworld", "Hexxit World", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("iChun_WorldPortals", "iChun", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ic2", "IndustrialCraft 2", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("icbmclassic", "ICBMClassic", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("iceandfire", "Ice and Fire", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("iceandfire:main_chan", "Ice and Fire", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ichunutil", "iChunUtil", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("immersiveengineering", "Immersive Engineering", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("immersivepetroleum", "Immersive Petroleum", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("immersivetech", "Immersive Tech", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("industrialforegoing", "IndustrialForegoing", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("industrialforegoing:", "Industrial Foregoing", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("inspirations", "Inspirations", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("inventorypets:channe", "InventoryPets", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("inventorysorter", "Inventory Sorter", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ip", "Inventory Pets", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ip.biomefinder", "Inventory Pets", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ip.biomeloc", "Inventory Pets", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ip.keyboard", "Inventory Pets", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ip.petnamer", "Inventory Pets", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ironchest", "IronChest", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ironchest:network", "IronChest", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("itemdamagerebalancer", "Item Damage Rebalancer", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("itemfilters", "Item Filters", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("jecalculation", "Just Enough Calculation", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("jee", "JustEnoughEnergistics", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("jei", "JustEnoughItems", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("jei:channel", "JustEnoughItems", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("jei:cheat_permission", "JustEnoughItems", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("jeid", "JustEnoughIDs", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("jm_dim_permission", "JourneyMap", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("jm_init_login", "JourneyMap", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("journeymap_channel", "JourneyMap", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("justenoughdrags", "JustEnoughDrags", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("kimetsuanimationplay", "Kimetsu Animation Player", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("kimetsunoallied:kime", "Kimetsuno Allied", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("kimetsunoyaiba:kimet", "Kimetsuno Yaiba", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("labymod:neo/addons/l", "LabyMod Minimap", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("labymod:neo/addons/labysminimap", "LabyMod Minimap", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ladylib", "LadyLib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("lain|nm|cos", "Cosmetic Armor Reworked", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("lazierae2:network", "Lazier AE 2", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("lethal_peaceful:leth", "Lethal Peaceful", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("levelupcfg", "Level Up! Reloaded", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("levelupclasses", "Level Up! Reloaded", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("levelupinit", "Level Up! Reloaded", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("levelupproperties", "Level Up! Reloaded", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("leveluprefresh", "Level Up! Reloaded", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("levelupskills", "Level Up! Reloaded", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("llibrary", "LLibrary", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("locks", "Locks", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("lootgames", "LootGames", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("lootr:main_network_c", "Lootr", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("lostcities", "Lost Cities", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("lycanitesmobs", "Lycanite's Mobs", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ma-enchants:ma-encha", "Ma Enchants", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("mahoutsukai:main_cha", "Mahout Sukai", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("mantle:books", "Mantle", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("mantle:network", "Mantle", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("mcb", "Minecraft Breakdown", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("mcjtylib:mcjtylib", "McJtyLib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("mcjtylib_ng", "McJtyLib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("mcmultipart", "Forge", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("mcmultipart_cbe", "Forge", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("mekanism:mekanism", "Mekanism", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("mekanismgenerators:m", "Mekanism Generators", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("mia_NETWORK", "Minor Integrations and Additions", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("mineminenomi:main_ch", "Mine Mine no Mi", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("mob_grinding_utils:n", "Mob Grinding Utils", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("mobends", "Mo' Bends", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("mod_lavacow", "Fish's Undead Rising", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("modularrouters", "ModularRouters", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("moremystcraft", "More MystCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("mowziesmobs", "Mowzie's Mobs", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("mowziesmobs:net", "Mowzie's Mobs", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("mrtjpcore", "MrTJPCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("mtrm", "MineTweaker Recipe Maker", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("mysticalagriculture:", "Mystical Agriculture", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("mysticallib", "MysticalLib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("naturescompass", "NaturesCompass", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("naturescompass:natur", "Nature's Compass", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("noxesium-v1:change_s", "Noxesium", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("noxesium-v1:mcc_game", "Noxesium", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("noxesium-v1:mcc_serv", "Noxesium", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("noxesium-v1:reset", "Noxesium", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("noxesium-v1:reset_se", "Noxesium", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("noxesium-v1:server_i", "Noxesium", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("noxesium-v2:change_e", "Noxesium", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("noxesium-v2:change_s", "Noxesium", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("noxesium-v2:mcc_game", "Noxesium", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("noxesium-v2:mcc_serv", "Noxesium", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("noxesium-v2:modify_s", "Noxesium", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("noxesium-v2:reset", "Noxesium", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("noxesium-v2:reset_ex", "Noxesium", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("noxesium-v2:reset_se", "Noxesium", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("noxesium-v2:server_i", "Noxesium", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("noxesium-v2:start_so", "Noxesium", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("noxesium-v2:stop_sou", "Noxesium", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("noxesium:server_rule", "Noxesium", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("numina", "Numina", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("nyx", "Nyx", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ob_aquamirae:ob_aqua", "OB Aquamirae", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ob_core:ob_core", "OBCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ob_tooltips:ob_toolt", "OB Tooltips", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("obfuscate:handshake", "Obfuscate", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("obfuscate:play", "Obfuscate", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("oe", "Oceanic Expanse", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("omegaconfig:sync", "OmegaConfig", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("omlib", "OMLib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("one_piece_seeker:one", "One Piece Seeker", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("one_piece_seeker_gog", "One Piece Seeker", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("openmodularturrets", "Open Modular Turrets", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("openterraingenerator", "OpenTerrainGenerator", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("originalbreath:origi", "Original Breath", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("pages", "Pages", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("pandoras_creatures:n", "Pandora's Creatures", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("patchouli", "Patchouli", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("patchouli:main", "Patchouli", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ping", "Ping", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("pixelmon", "Pixelmon Reforged", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("placebo:placebo", "Placebo", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("planetprogression", "PlanetProgression", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("plethora", "Plethora", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("plustic", "Plustic", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("potioncore", "Potion Core", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("projecte", "ProjectE", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("projecte:main_channe", "ProjectE", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("projectred-core", "ProjectRed", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("projectred-expansion", "ProjectRed", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("puzzleslib:main", "PuzzlesLib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("qualitytools", "Quality Tools", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("quark:main", "Quark", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("quarryplus:main", "Quarry Plus", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("quarryplus:marker", "Quarry Plus", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("rc&reborncore.&39932", "RebornCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("rc&reborncore.&64769", "RebornCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("rc&techreborn.&42258", "RebornCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("rc&vswe.steves&10928", "RebornCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("reachfix", "ReachFix", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("reccomplex", "RecComplex", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("recipemod:key", "YARCF", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("refinedstorage", "RefinedStorage", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("refinedstorage:main_", "Refined Storage", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("reforged", "Reforged", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("replaymod:restrict", "Replay Mod", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("reskillable", "Reskillable", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("rftoolsbase:rftoolsb", "RFTools", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("rftoolsdim:rftoolsdi", "RFTools", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("rftoolspower:rftools", "RFTools", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("rftoolspower", "RFTools", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("rftools", "RFTools", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("rlmixins", "RLMixins", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("rltweaker", "RLTweaker", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("rustic", "Rustic", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("sampler", "Sampler", DataType.REGISTER_DATA_MOD, false),

            new PayloadType("sevtweaks", "SevTweaks", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("modularmachinery", "Modular Machinery", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ironbackpacks", "Iron Backpacks", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("betterwithaddons", "Better With Addons", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("RID_CHAN", "Realistic Item Drops", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("jtp", "Unknown (jtp)", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BNBClientToServer", "BNBGamingLib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BNBServerToClient", "BNBGamingLib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("emberroot", "EmberRoot Zoo", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("4017", "CD4017BE_lib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("4017s", "CD4017BE_lib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("4017g", "CD4017BE_lib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("primal", "PrimalCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("magma_monsters", "Magma Monsters", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("immcraft", "Immersive Craft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("refinedstorageaddons", "Refined Storage Addons", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("car", "Ultimate Car Mod", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("nutrition", "Nutrition", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("progressiontweaks", "Progression Tweaks", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("scannable", "Scannable", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("overloaded", "Overloaded", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("genplayerskins", "Player Skin Mobs", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("quantumstorage", "QuantumStorage", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("rftoolsctrl", "RFTools Control", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("death_compass", "death_compass", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("environmentaltech", "Environmental Tech", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("primal_tech", "primal_tech", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("abyssalcraft", "AbyssalCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("igwmod", "In-Game wiki Mod", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("actuallyadditions", "Actually Additions", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("tothebatpoles", "To The Bat Poles!", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("waterstrainer", "Water Strainer", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("improvedbackpacks", "Improved Backpacks", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("sevtweaks", "SevTweaks", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("jaff", "Just a Few Fish", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("betterwithmods", "Better With Mods", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("bdew.generators", "Advanced Generators", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("totemic", "Totemic", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("thebetweenlands", "The Betweenlands", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("rc&me.modmuss5&21645", "Reborn Core", DataType.REGISTER_DATA_MOD, false),

            new PayloadType("slabmachines", "Slab Machines", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cyclopscore", "Cyclops Core", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("extendedcrafting", "Extended Crafting", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("chineseworkshop", "ChineseWorkshop", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("genmpbasic", "ModPack Basic Tools", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("apotheosis", "Apotheosis", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("sqpatch", "Squeezer Patch", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("mob_grinding_utils", "Mob Grinding Utils", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("commoncapabilities", "CommonCapabilities", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("gamestages", "Game Stages", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("integratedtunnels", "Integrated Tunnels", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("tp", "TelePastries", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("SKY_CHAN", "Unknown (SKY_CHAN)", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("genmputils", "ModPack Basic Tools", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("pipemaster", "Pipe Master 2000", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("realfilingcabinet", "Real Filing Cabinet", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("Sync", "Sync", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("compactmachines3", "Compact Machines 3", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("clickmachine", "Click Machine", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("bonsaitrees", "Bonsai Trees", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("cyclicmagic", "Cyclic", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("integrateddynamics", "Integrated Dynamics", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("pickletweaks", "Pickle Tweaks", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("integrateddynamicsco", "Integrated Dynamics Compat", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("findme", "FindMe", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ironjetpacks", "Iron Jetpacks", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("darkutils", "Dark Utilities", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("malisiscore", "MalisisCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("BDSM_CHAN", "Barrels, Drums, Storage & More", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("deepmoblearning", "Deep Mob Learning", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("PrimitiveCrafting", "Primitive Crafting", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("simplegenerators", "Simple Generators", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("horsepower", "Horse Power", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("walljump", "Wall-Jump!", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("network", "Unknown (Network)", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("worldutils", "World Utils", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("practicallogistics2", "Practical Logistics 2", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("Astral Sorcery", "Astral Sorcery", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("buildinggadgets", "Building Gadgets", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("limitlessstructureblocks", "Limitless Structure Blocks", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("malisisdoors", "MalisisDoors", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("valkyrielib", "Valkyrie Lib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("parabox", "Parabox", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("ceramics", "Ceramics", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("enderutilities", "Ender Utilities", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("TGS2014", "Tree Growing Simulator 2016", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("integratedtunnelscom", "Integrated Tunnels Compat", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("yoyos", "Yoyos", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("nuclearcraft", "NuclearCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("storagenetwork", "Simple Storage Network", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("topography", "Topography", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("prestige", "Prestige", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("vc", "ViesCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("compactstorage", "CompactStorage", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("mo_channel", "Unknown (mo_channel)", DataType.REGISTER_DATA_MOD, false),

            new PayloadType("scalinghealth", "Scaling Health", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("schematica", "Schematica", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("secretroomsmod", "Secret Rooms 5", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("selene:network", "Selene", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("sereneseasons", "Serene Seasons", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("setbonus", "Set Bonus", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("shetiphiancore:main_", "Shetiphian Core", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("silentlib", "SilentLib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("simpledifficulty", "Simple Difficulty", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("simplyjetpacks", "SimplyJetpacks", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("skinport", "SkinPort", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("skyvillages:skyvilla", "Sky Villages", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("slimyboyos", "SlimyBoyos", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("smallships:default", "Small Ships Mod", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("sme.net", "sme.net", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("sons_of_sins:sons_of", "Sons of Sins", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("spartanshields", "Spartan Shields", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("spartanshields:netwo", "Spartan Shields", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("spartanweaponry", "Spartan Weaponry", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("spartanweaponry:netw", "Spartan Weaponry", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("statues", "Statues", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("storagedrawers", "StorageDrawers", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("storagedrawers:main_", "Storage Drawers", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("structure_gel:main", "Structure Gel", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("superfactorymanager", "SuperFactoryManager", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("supermartijn642confi", "Supermartijn642's Mods", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("supersoundmuffler", "SuperSoundMuffler", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("supplementaries:netw", "Supplementaries", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("switchbow", "Switch-Bow", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("tcg", "Pixelmon Reforged", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("tcinventoryscan", "Thaumcraft Inventory Scanning", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("tcomplement", "Tinkers Complement", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("tconstruct", "Tinkers Construct", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("tconstruct:network", "Tinkers Construct", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("teamlapenlib:main", "TeamLapenLib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("teslacorelib", "TeslaCoreLib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("thaumcraft", "ThaumCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("thaumicaugmentation", "ThaumCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("thaumicenergistics", "ThaumCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("thaumictinkerer", "ThaumCraft", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("thaumicwonders", "Thaumic Wonders", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("the5zigmod:5zig", "The 5zig Mod", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("the5zigmod:5zig_reg", "The 5zig Mod", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("the5zigmod:5zig_set", "The 5zig Mod", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("tinkerlevel:sync", "Tinkers Tool Leveling", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("titanium:network", "Titanium", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("tombmanygraves", "Tomb Many Graves", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("tombstone:tombstone_", "Tombstone", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("toolbelt", "Toolbelt", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("torchmaster", "TorchMaster", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("translocators", "Translocators", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("trashslot", "TrashSlot", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("travelersbackpack", "Traveler's Backpack", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("treechop-channel", "TreeChop", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("tweed4:sync_config", "Tweed Config", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("twilightforest", "TwilightForest", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("twilightforest:chann", "Twilight Forest", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("uniquebase:networkin", "Unique Enchantments", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("universaltweaks", "Universal Tweaks", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("usefulbackpacks:curi", "Useful Backpacks", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("uteamcore:network", "UTeamCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("valkyrielib:main", "ValkyrieLib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("vampirism:main", "Vampirism", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("variedcommodities", "Varied Commodities", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("vending", "Vending", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("vnChannel", "Village Names", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("voicechat:add_catego", "Simple Voice Chat", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("voicechat:add_group", "Simple Voice Chat", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("voicechat:default", "Simple Voice Chat", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("voicechat:joined_gro", "Simple Voice Chat", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("voicechat:player_sta", "Simple Voice Chat", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("voicechat:remove_cat", "Simple Voice Chat", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("voicechat:remove_gro", "Simple Voice Chat", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("voicechat:secret", "Simple Voice Chat", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("waila", "WAILA", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("waila:networking", "WAILA", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("wanionlib", "WanionLib", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("waystones", "Waystones", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("waystones:network", "Waystones", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("wearablebackpacks", "Wearable Backpacks", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("werewolves:main", "Werewolves", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("wolfarmor", "Wolf Armor and Storage", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("wonderful_enchantmen", "Wonderful Enchantments", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("world_id", "VoxelMap/JourneyMap", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("world_info", "VoxelMap/JourneyMap", DataType.REGISTER_DATA_OTHER, false),
            new PayloadType("worldedit:cui", "WorldEdit CUI", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("worsebarrels", "Worse Barrels", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("xaerominimap:main", "Xaero's Minimap", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("xaeroworldmap:main", "Xaero's World Map", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("xat", "Trinkets and Baubles", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("xnet", "XNet", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("xreliquary", "Reliquary", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("xreliquary:channel", "XReliquary", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("zerocore:network", "ZeroCore", DataType.REGISTER_DATA_MOD, false),
            new PayloadType("zettaindustries", "ZettaIndustries", DataType.REGISTER_DATA_MOD, false),

            new PayloadType("eosclient:a", "Eos Client", DataType.REGISTER_DATA_OTHER, true),
            new PayloadType("Lunar-Client", "Lunar Client Spoof", DataType.REGISTER_DATA_OTHER, true),
            new PayloadType("\fLunar-Client", "Lunar Client Spoof", DataType.REGISTER_DATA_OTHER, true)
    );

    private static final List<PayloadType> CHANNELS = Arrays.asList(
            new PayloadType("ironjetpacks", "Iron Jetpacks", DataType.CHANNEL, false),
            new PayloadType("Astral Sorcery", "Astral Sorcery", DataType.CHANNEL, false),
            new PayloadType("BSM", "Better Sprinting", DataType.CHANNEL, false),
            new PayloadType("essential|diffct", "Essential Mod", DataType.CHANNEL, false),
            new PayloadType("AE2", "AE2", DataType.CHANNEL, false),
            new PayloadType("AS_IF", "ASMC", DataType.CHANNEL, false),
            new PayloadType("AS_MM", "ASMC", DataType.CHANNEL, false),
            new PayloadType("Animania", "Animania", DataType.CHANNEL, false),
            new PayloadType("BIN", "BinnieMods", DataType.CHANNEL, false),
            new PayloadType("BOT", "Botania", DataType.CHANNEL, false),
            new PayloadType("BQ_NET_CHAN", "Better Questing", DataType.CHANNEL, false),
            new PayloadType("BiblioAStand", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioAtlas", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioAtlasSWP", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioAtlasTGUI", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioAtlasWPT", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioClipboard", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioClock", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioDeskOpenGUI", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioDrillText", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioMCBEdit", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioMCBPage", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioMapPin", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioMeasure", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioOpenBook", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioPaintPress", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioPainting", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioPaintingC", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioPaneler", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioRBook", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioRBookLoad", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioRecipeCraft", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioRecipeText", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioRenderUpdate", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioSign", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioStockCompass", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioStockLog", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioStockTitle", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioType", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioTypeDelete", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioTypeFlag", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioTypeUpdate", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("BiblioUpdateInv", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("CCL_INTERNAL", "CodeChicken Lib", DataType.CHANNEL, false),
            new PayloadType("CarryOn", "Carry On", DataType.CHANNEL, false),
            new PayloadType("ChickenChunks", "ChickenChunks", DataType.CHANNEL, false),
            new PayloadType("ChiselsAndBits", "ChiselsAndBits", DataType.CHANNEL, false),
            new PayloadType("CoFH", "CoFHCore", DataType.CHANNEL, false),
            new PayloadType("CustomNPCsPlayer", "Custom NPCs", DataType.CHANNEL, false),
            new PayloadType("EnderCore", "EnderCore", DataType.CHANNEL, false),
            new PayloadType("FLoco", "FunkyLocomotion", DataType.CHANNEL, false),
            new PayloadType("FML|HS", "Forge", DataType.CHANNEL, false),
            new PayloadType("FOR", "Forge", DataType.CHANNEL, false),
            new PayloadType("FlansMod", "Flan's Mod", DataType.CHANNEL, false),
            new PayloadType("ForgeMicroblock", "Forge", DataType.CHANNEL, false),
            new PayloadType("ForgeMultipart", "Forge", DataType.CHANNEL, false),
            new PayloadType("GuideAPI", "GuideAPI", DataType.CHANNEL, false),
            new PayloadType("HatStand", "HatStand", DataType.CHANNEL, false),
            new PayloadType("Hats", "Hats", DataType.CHANNEL, false),
            new PayloadType("InventoryTweaks", "InventoryTweaks", DataType.CHANNEL, false),
            new PayloadType("LogisticsPipes", "LogisticsPipes", DataType.CHANNEL, false),
            new PayloadType("MC|AdvCdm", "Minecraft", DataType.CHANNEL, false),
            new PayloadType("MC|BEdit", "Minecraft", DataType.CHANNEL, false),
            new PayloadType("MC|BSign", "Minecraft", DataType.CHANNEL, false),
            new PayloadType("MC|Beacon", "Minecraft", DataType.CHANNEL, false),
            new PayloadType("MC|ItemName", "Minecraft", DataType.CHANNEL, false),
            new PayloadType("MC|PickItem", "Minecraft", DataType.CHANNEL, false),
            new PayloadType("MC|TrSel", "Minecraft", DataType.CHANNEL, false),
            new PayloadType("MEK", "Mekanism", DataType.CHANNEL, false),
            new PayloadType("MorePlayerModels", "MorePlayerModels", DataType.CHANNEL, false),
            new PayloadType("Morph", "Morph", DataType.CHANNEL, false),
            new PayloadType("Mystcraft", "Mystcraft", DataType.CHANNEL, false),
            new PayloadType("OCMC", "OpticCraft Client", DataType.CHANNEL, false),
            new PayloadType("OpenComputers", "OpenComputers", DataType.CHANNEL, false),
            new PayloadType("OpenMods|E", "OpenMods", DataType.CHANNEL, false),
            new PayloadType("OpenMods|M", "OpenMods", DataType.CHANNEL, false),
            new PayloadType("OpenMods|RPC", "OpenMods", DataType.CHANNEL, false),
            new PayloadType("OpenTerrainGenerator", "OpenTerrainGenerator", DataType.CHANNEL, false),
            new PayloadType("PERMISSIONSREPL", "WorldDownloader", DataType.CHANNEL, false),
            new PayloadType("PR|Integr", "ProjectRed", DataType.CHANNEL, false),
            new PayloadType("PR|Reloc", "ProjectRed", DataType.CHANNEL, false),
            new PayloadType("PR|Transp", "ProjectRed", DataType.CHANNEL, false),
            new PayloadType("PneumaticCraft", "PneumaticCraft", DataType.CHANNEL, false),
            new PayloadType("PortalGun", "PortalGun", DataType.CHANNEL, false),
            new PayloadType("RC", "RebornCore", DataType.CHANNEL, false),
            new PayloadType("Schematica", "Schematica", DataType.CHANNEL, false),
            new PayloadType("TeamWizardry", "LibrarianLib", DataType.CHANNEL, false),
            new PayloadType("TestDummy", "TestDummy", DataType.CHANNEL, false),
            new PayloadType("WDL|INIT", "WorldDownloader", DataType.CHANNEL, false),
            new PayloadType("WECUI", "WorldEdit CUI", DataType.CHANNEL, false),
            new PayloadType("WRCBE", "WRCBE", DataType.CHANNEL, false),
            new PayloadType("WorldControl", "WorldControl", DataType.CHANNEL, false),
            new PayloadType("XU2", "Extra Utilities 2", DataType.CHANNEL, false),
            new PayloadType("abnormals_core:net", "Abnormal's Core", DataType.CHANNEL, false),
            new PayloadType("advancedcapes", "AdvancedCapes", DataType.CHANNEL, false),
            new PayloadType("aether_legacy", "Aether Legacy", DataType.CHANNEL, false),
            new PayloadType("appleskin", "AppleSkin", DataType.CHANNEL, false),
            new PayloadType("aroma1997core", "Aroma1997", DataType.CHANNEL, false),
            new PayloadType("aroma1997sdimension", "Aroma1997", DataType.CHANNEL, false),
            new PayloadType("astikorcarts", "Astikor Carts", DataType.CHANNEL, false),
            new PayloadType("autoreglib", "AutoRegLib", DataType.CHANNEL, false),
            new PayloadType("backpack", "Backpacks", DataType.CHANNEL, false),
            new PayloadType("baubles", "Baubles", DataType.CHANNEL, false),
            new PayloadType("bbwands", "Better Builder's Wands", DataType.CHANNEL, false),
            new PayloadType("bdew.ae2stuff", "Bdew", DataType.CHANNEL, false),
            new PayloadType("bdew.compacter", "Bdew", DataType.CHANNEL, false),
            new PayloadType("bdew.multiblock", "Bdew", DataType.CHANNEL, false),
            new PayloadType("betteranimalsplus", "BetterAnimalsPlus", DataType.CHANNEL, false),
            new PayloadType("bettercombatmod", "RLCombat", DataType.CHANNEL, false),
            new PayloadType("bibliocraft", "BiblioCraft", DataType.CHANNEL, false),
            new PayloadType("bigreactors", "BigReactors", DataType.CHANNEL, false),
            new PayloadType("biomecolorizer:a", "BiomeColorizer", DataType.CHANNEL, false),
            new PayloadType("biomesoplenty", "Biomes O' Plenty", DataType.CHANNEL, false),
            new PayloadType("chunkpregenerator", "Chunk Pregenerator", DataType.CHANNEL, false),
            new PayloadType("bloodmagic", "Blood Magic", DataType.CHANNEL, false),
            new PayloadType("botania", "Botania", DataType.CHANNEL, false),
            new PayloadType("buildcraftbuilders", "BuildCraft", DataType.CHANNEL, false),
            new PayloadType("buildcraftcore", "BuildCraft", DataType.CHANNEL, false),
            new PayloadType("buildcraftlib", "BuildCraft", DataType.CHANNEL, false),
            new PayloadType("buildcraftrobotics", "BuildCraft", DataType.CHANNEL, false),
            new PayloadType("buildcrafttransport", "BuildCraft", DataType.CHANNEL, false),
            new PayloadType("carryon:carryonpacke", "CarryOn", DataType.CHANNEL, false),
            new PayloadType("cfm", "FurnitureMod", DataType.CHANNEL, false),
            new PayloadType("chisel", "Chisel", DataType.CHANNEL, false),
            new PayloadType("chrs:lib", "Charset", DataType.CHANNEL, false),
            new PayloadType("chrs:pocket", "Charset", DataType.CHANNEL, false),
            new PayloadType("collisiondamage", "CollisionDamage", DataType.CHANNEL, false),
            new PayloadType("computercraft", "ComputerCraft", DataType.CHANNEL, false),
            new PayloadType("computronics", "Computronics", DataType.CHANNEL, false),
            new PayloadType("cookingforblockheads", "CookingForBlockheads", DataType.CHANNEL, false),
            new PayloadType("cosmeticarmorreworke", "Cosmetic Armor Reworked", DataType.CHANNEL, false),
            new PayloadType("craftstudioapi", "CraftStudioAPI", DataType.CHANNEL, false),
            new PayloadType("crafttweaker", "CraftTweaker", DataType.CHANNEL, false),
            new PayloadType("creativemd", "CreativeMD", DataType.CHANNEL, false),
            new PayloadType("cucumber", "Cucumber", DataType.CHANNEL, false),
            new PayloadType("culinaryconstruct", "CulinaryConstruct", DataType.CHANNEL, false),
            new PayloadType("damagetilt", "DamageTilt", DataType.CHANNEL, false),
            new PayloadType("eiococ", "EnderIO", DataType.CHANNEL, false),
            new PayloadType("elenaidodge", "Elenai Dodge", DataType.CHANNEL, false),
            new PayloadType("enderio", "EnderIO", DataType.CHANNEL, false),
            new PayloadType("enderioconduits", "EnderIO", DataType.CHANNEL, false),
            new PayloadType("enderioinvpanel", "EnderIO", DataType.CHANNEL, false),
            new PayloadType("enderiomachines", "EnderIO", DataType.CHANNEL, false),
            new PayloadType("enderiopowertools", "EnderIO", DataType.CHANNEL, false),
            new PayloadType("energycontrol", "Energy Control", DataType.CHANNEL, false),
            new PayloadType("eplus", "EnchantingPlus", DataType.CHANNEL, false),
            new PayloadType("exchangers", "Exchangers", DataType.CHANNEL, false),
            new PayloadType("extrabitmanipulation", "Extra Bit Manipulation", DataType.CHANNEL, false),
            new PayloadType("extracells", "ExtraCells", DataType.CHANNEL, false),
            new PayloadType("extraplanets", "ExtraPlanets", DataType.CHANNEL, false),
            new PayloadType("fabric:container/ope", "Fabric", DataType.CHANNEL, false),
            new PayloadType("fabric:registry/sync", "Fabric", DataType.CHANNEL, false),
            new PayloadType("factorymanager", "FactoryManager", DataType.CHANNEL, false),
            new PayloadType("fairylights", "FairyLights", DataType.CHANNEL, false),
            new PayloadType("fancymenu:variable_c", "FancyMenu", DataType.CHANNEL, false),
            new PayloadType("fastbench", "FastWorkbench", DataType.CHANNEL, false),
            new PayloadType("feather:client", "Feather Client", DataType.CHANNEL, false),
            new PayloadType("firstaid", "First Aid", DataType.CHANNEL, false),
            new PayloadType("forgemultipartcbe", "Forge", DataType.CHANNEL, false),
            new PayloadType("ftbutilities", "FTB Utilities", DataType.CHANNEL, false),
            new PayloadType("futuremc", "FutureMC", DataType.CHANNEL, false),
            new PayloadType("galacticraft", "GalactiCraft", DataType.CHANNEL, false),
            new PayloadType("galacticraftcore", "GalactiCraft", DataType.CHANNEL, false),
            new PayloadType("gasconduits", "GasConduits", DataType.CHANNEL, false),
            new PayloadType("hammercore2", "Hammer Core", DataType.CHANNEL, false),
            new PayloadType("harvestcraft", "Pam's HarvestCraft", DataType.CHANNEL, false),
            new PayloadType("iChun_WorldPortals", "iChun", DataType.CHANNEL, false),
            new PayloadType("ic2", "IndustrialCraft 2", DataType.CHANNEL, false),
            new PayloadType("icbmclassic", "ICBMClassic", DataType.CHANNEL, false),
            new PayloadType("ichunutil", "iChunUtil", DataType.CHANNEL, false),
            new PayloadType("industrialforegoing", "IndustrialForegoing", DataType.CHANNEL, false),
            new PayloadType("ironchest", "IronChest", DataType.CHANNEL, false),
            new PayloadType("jee", "JustEnoughEnergistics", DataType.CHANNEL, false),
            new PayloadType("jei", "JustEnoughItems", DataType.CHANNEL, false),
            new PayloadType("jeid", "JustEnoughItems", DataType.CHANNEL, false),
            new PayloadType("jm_dim_permission", "JourneyMap", DataType.CHANNEL, false),
            new PayloadType("jm_init_login", "JourneyMap", DataType.CHANNEL, false),
            new PayloadType("journeymap_channel", "JourneyMap", DataType.CHANNEL, false),
            new PayloadType("kimetsunoyaiba:kimet", "Kimetsuno Yaiba", DataType.CHANNEL, false),
            new PayloadType("labymod3:main", "LabyMod", DataType.CHANNEL, false),
            new PayloadType("labymod:neo", "LabyMod", DataType.CHANNEL, false),
            new PayloadType("lain|nm|cos", "Cosmetic Armor Reworked", DataType.CHANNEL, false),
            new PayloadType("levelupclasses", "Level Up! Reloaded", DataType.CHANNEL, false),
            new PayloadType("lunar:apollo", "Lunar Client", DataType.CHANNEL, false),
            new PayloadType("lycanitesmobs", "Lycanite's Mobs", DataType.CHANNEL, false),
            new PayloadType("ma-enchants:ma-encha", "Ma Enchants", DataType.CHANNEL, false),
            new PayloadType("mantle:books", "Mantle", DataType.CHANNEL, false),
            new PayloadType("mcmultipart", "Forge", DataType.CHANNEL, false),
            new PayloadType("mcmultipart_cbe", "Forge", DataType.CHANNEL, false),
            new PayloadType("minecraft:intave", "LabyMod", DataType.CHANNEL, false),
            new PayloadType("minecraft:treechop-c", "TreeChop", DataType.CHANNEL, false),
            new PayloadType("mobends", "Mo' Bends", DataType.CHANNEL, false),
            new PayloadType("modularrouters", "ModularRouters", DataType.CHANNEL, false),
            new PayloadType("mowziesmobs", "Mowzie's Mobs", DataType.CHANNEL, false),
            new PayloadType("mrtjpcore", "MrTJPCore", DataType.CHANNEL, false),
            new PayloadType("mysticallib", "MysticalLib", DataType.CHANNEL, false),
            new PayloadType("naturescompass", "NaturesCompass", DataType.CHANNEL, false),
            new PayloadType("noxesium:client_info", "Noxesium", DataType.CHANNEL, false),
            new PayloadType("noxesium:client_sett", "Noxesium", DataType.CHANNEL, false),
            new PayloadType("numina", "Numina", DataType.CHANNEL, false),
            new PayloadType("openterraingenerator", "OpenTerrainGenerator", DataType.CHANNEL, false),
            new PayloadType("originalbreath:origi", "Original Breath", DataType.CHANNEL, false),
            new PayloadType("patchouli", "Patchouli", DataType.CHANNEL, false),
            new PayloadType("ping", "Ping", DataType.CHANNEL, false),
            new PayloadType("pixelmon", "Pixelmon Reforged", DataType.CHANNEL, false),
            new PayloadType("planetprogression", "PlanetProgression", DataType.CHANNEL, false),
            new PayloadType("plethora", "Plethora", DataType.CHANNEL, false),
            new PayloadType("plustic", "Plustic", DataType.CHANNEL, false),
            new PayloadType("projecte", "ProjectE", DataType.CHANNEL, false),
            new PayloadType("projectred-core", "ProjectRed", DataType.CHANNEL, false),
            new PayloadType("projectred-expansion", "ProjectRed", DataType.CHANNEL, false),
            new PayloadType("rc&reborncore.&39932", "RebornCore", DataType.CHANNEL, false),
            new PayloadType("rc&reborncore.&64769", "RebornCore", DataType.CHANNEL, false),
            new PayloadType("rc&techreborn.&42258", "RebornCore", DataType.CHANNEL, false),
            new PayloadType("rc&vswe.steves&10928", "RebornCore", DataType.CHANNEL, false),
            new PayloadType("reccomplex", "RecComplex", DataType.CHANNEL, false),
            new PayloadType("recipemod:key", "NoMoreRecipeConflict", DataType.CHANNEL, false),
            new PayloadType("reskillable", "Reskillable", DataType.CHANNEL, false),
            new PayloadType("sampler", "Sampler", DataType.CHANNEL, false),
            new PayloadType("schematica", "Schematica", DataType.CHANNEL, false),
            new PayloadType("secretroomsmod", "Secret Rooms 5", DataType.CHANNEL, false),
            new PayloadType("simplyjetpacks", "SimplyJetpacks", DataType.CHANNEL, false),
            new PayloadType("skinport", "SkinPort", DataType.CHANNEL, false),
            new PayloadType("spartanweaponry", "Spartan Weaponry", DataType.CHANNEL, false),
            new PayloadType("statues", "Statues", DataType.CHANNEL, false),
            new PayloadType("storagedrawers", "StorageDrawers", DataType.CHANNEL, false),
            new PayloadType("superfactorymanager", "SuperFactoryManager", DataType.CHANNEL, false),
            new PayloadType("supersoundmuffler", "SuperSoundMuffler", DataType.CHANNEL, false),
            new PayloadType("tcg", "Pixelmon Reforged", DataType.CHANNEL, false),
            new PayloadType("tconstruct", "TinkersConstruct", DataType.CHANNEL, false),
            new PayloadType("teamlapenlib:main", "TeamLapenLib", DataType.CHANNEL, false),
            new PayloadType("teslacorelib", "TeslaCoreLib", DataType.CHANNEL, false),
            new PayloadType("thaumcraft", "ThaumCraft", DataType.CHANNEL, false),
            new PayloadType("carbonconfig", "Carbon Config", DataType.CHANNEL, false),
            new PayloadType("thaumicaugmentation", "ThaumCraft", DataType.CHANNEL, false),
            new PayloadType("thaumicenergistics", "ThaumCraft", DataType.CHANNEL, false),
            new PayloadType("thaumictinkerer", "ThaumCraft", DataType.CHANNEL, false),
            new PayloadType("the5zigmod:5zig", "The 5zig Mod", DataType.CHANNEL, false),
            new PayloadType("the5zigmod:5zig_reg", "The 5zig Mod", DataType.CHANNEL, false),
            new PayloadType("the5zigmod:5zig_set", "The 5zig Mod", DataType.CHANNEL, false),
            new PayloadType("torchmaster", "TorchMaster", DataType.CHANNEL, false),
            new PayloadType("translocators", "Translocators", DataType.CHANNEL, false),
            new PayloadType("trashslot", "TrashSlot", DataType.CHANNEL, false),
            new PayloadType("travelersbackpack", "Traveler's Backpack", DataType.CHANNEL, false),
            new PayloadType("treechop-channel", "TreeChop", DataType.CHANNEL, false),
            new PayloadType("twilightforest", "TwilightForest", DataType.CHANNEL, false),
            new PayloadType("uniquebase:networkin", "Unique Enchantments", DataType.CHANNEL, false),
            new PayloadType("voicechat:request_se", "Simple Voice Chat", DataType.CHANNEL, false),
            new PayloadType("waila", "WAILA", DataType.CHANNEL, false),
            new PayloadType("wanionlib", "WanionLib", DataType.CHANNEL, false),
            new PayloadType("waystones", "Waystones", DataType.CHANNEL, false),
            new PayloadType("wonderful_enchantmen", "Wonderful Enchantments", DataType.CHANNEL, false),
            new PayloadType("world_id", "VoxelMap/JourneyMap", DataType.CHANNEL, false),
            new PayloadType("world_info", "VoxelMap/JourneyMap", DataType.CHANNEL, false),
            new PayloadType("worldedit:cui", "WorldEdit CUI", DataType.CHANNEL, false),
            new PayloadType("xat", "Trinkets and Baubles", DataType.CHANNEL, false),
            new PayloadType("xreliquary", "Reliquary", DataType.CHANNEL, false),
            new PayloadType("zettaindustries", "ZettaIndustries", DataType.CHANNEL, false),

            new PayloadType("eosclient:a", "Eos Client", DataType.CHANNEL, true),
            new PayloadType("#unbanearwax", "Vape Client", DataType.CHANNEL, true),
            new PayloadType("0SO1Lk2KASxzsd", "BspkrsCore Client", DataType.CHANNEL, true),
            new PayloadType("0SSxzsd", "Random Client", DataType.CHANNEL, true),
            new PayloadType("1946203560", "Vape Client", DataType.CHANNEL, true),
            new PayloadType("Aimbot", "Merge Client", DataType.CHANNEL, true),
            new PayloadType("Mystra-B1", "Mystra B1", DataType.CHANNEL, true),
            new PayloadType("BLC|M", "Remix Client", DataType.CHANNEL, true),
            new PayloadType("EARWAXWASHERE", "Vape Client", DataType.CHANNEL, true),
            new PayloadType("EROUAXWASHERE", "Vape Client", DataType.CHANNEL, true),
            new PayloadType("CPS_BAN_THIS_NIGGER", "Vape Client", DataType.CHANNEL, true),
            new PayloadType("LOLIMAHCKER", "Vape Client", DataType.CHANNEL, true),
            new PayloadType("Lunar-Client", "Lunar Client Spoof", DataType.CHANNEL, true),
            new PayloadType("MCnetHandler", "TimeChanger Misplace", DataType.CHANNEL, true),
            new PayloadType("XDSMKDKFDKSDAKDFkEJF", "Moon Client", DataType.CHANNEL, true),
            new PayloadType("cock", "Reach Mod", DataType.CHANNEL, true),
            new PayloadType("customGuiOpenBspkrs", "BspkrsCore Client", DataType.CHANNEL, true),
            new PayloadType("ethylene", "Ethylene Client", DataType.CHANNEL, true),
            new PayloadType("gc", "Generic Client", DataType.CHANNEL, true),
            new PayloadType("gg", "Reach Mod", DataType.CHANNEL, true),
            new PayloadType("lmaohax", "Reach Mod", DataType.CHANNEL, true),
            new PayloadType("mergeclient", "Merge Client", DataType.CHANNEL, true),
            new PayloadType("mincraftpvphcker", "BspkrsCore Client", DataType.CHANNEL, true),
            new PayloadType("n", "TimeChanger Misplace", DataType.CHANNEL, true),
            new PayloadType("nana", "Azurya Client", DataType.CHANNEL, true),
            new PayloadType("reach", "Reach Mod", DataType.CHANNEL, true),
            new PayloadType("wigger", "Merge Client", DataType.CHANNEL, true)
    );

    public ClientBrand(@NotNull PlayerData playerData) throws ClassNotFoundException {
        super(playerData);
    }

    @Override
    public void handle(@NotNull PacketPlayReceiveEvent event) {
        PacketTypeCommon packetType = event.getPacketType();

        if (packetType == PacketType.Play.Client.PLUGIN_MESSAGE) {
            @NotNull WrapperPlayClientPluginMessage payload = new WrapperPlayClientPluginMessage(event);
            byte[] rawData = payload.getData();
            @NotNull String data = new String(rawData, StandardCharsets.UTF_8).replace(" (Velocity)", "");
            @Nullable String channelName = payload.getChannelName();

            // Handles null channel names.
            if (channelName == null) {
                KickUtil.kickPlayer(player, event, "Sent payload with null channelName");
                return;
            }

            // Handles and validates the payload sent by the player.
            if (channelName.equals("minecraft:brand") || channelName.equals("MC|Brand")) {
                handleBrandData(data, event);
            } else if (channelName.equals("minecraft:register") || channelName.equals("REGISTER")) {
                handleRegisterData(data, event);
            } else {
                validateAndProcessPayload(CHANNELS, event, channelName, DataType.CHANNEL);
            }

            // Checks for blocked payloads registered by the player.
            checkForBlockedPayloads(event);

            // Checks for inconsistencies between version and brand data.
            checkForImpossibilities(event);
        }
    }

    /**
     * Handles the brand data sent by the player.
     *
     * @param data  The data sent by the player.
     * @param event The event to cancel.
     */
    private void handleBrandData(@NotNull String data, @NotNull CancellableEvent event) {
        // Kicks players sending the Log4J Exploit through their brand.
        if (data.contains("jndi") || data.contains("ldap")
                || data.contains("://") || data.contains("${")
                || data.contains("192.168")) {
            KickUtil.kickPlayer(player, event, "Blocked Brand: Log4J Exploit");
            return;
        }

        // Kicks players on Crystalware.
        if (data.contains("CRYSTAL|") || data.contains("Winterware")) {
            KickUtil.kickPlayer(player, event, "Blocked Brand: Crystalware");
            return;
        }

        // Kicks players that register empty brands.
        if (data.isEmpty()) {
            KickUtil.kickPlayer(player, event, "Empty Brand");
            return;
        }

        // Checks for Lunar Client.
        if (data.matches("\u0013lunarclient:.*$")
                || data.matches("\u0017lunarclient:.*$")
                || data.matches("\u0018lunarclient:.*$")) {
            playerData.getPayloads().add(new PayloadType(data, "Lunar Client", DataType.BRAND, false));
            return;
        }

        // Checks for CM Client.
        if (data.matches("\u0010cmclient:.*$")) {
            playerData.getPayloads().add(new PayloadType(data, "CM Client", DataType.BRAND, false));
            return;
        }

        // Checks the sent payload against the brand list.
        validateAndProcessPayload(BRANDS, event, data, DataType.BRAND);
    }

    /**
     * Handles the register data sent by the player.
     *
     * @param data  The data sent by the player.
     * @param event The event to cancel.
     */
    private void handleRegisterData(@NotNull String data, @NotNull CancellableEvent event) {
        // Kicks players that register empty data.
        if (data.isEmpty()) {
            KickUtil.kickPlayer(player, event, "Empty Data");
            return;
        }

        // Splits the null spaces from the data and checks lines individually.
        if (data.contains("\0")) {
            String @NotNull [] splitData = data.split("\0");

            for (@NotNull String line : splitData) {
                validateAndProcessPayload(REGISTER_DATA, event, line, DataType.REGISTER_DATA_OTHER);
            }
        } else {
            validateAndProcessPayload(REGISTER_DATA, event, data, DataType.REGISTER_DATA_OTHER);
        }
    }

    /**
     * Checks for blocked payloads registered by the player.
     *
     * @param event The event to cancel.
     */
    private void checkForBlockedPayloads(@NotNull CancellableEvent event) {
        // If the payload is blocked, kick the player.
        for (@NotNull PayloadType payloadType : playerData.getPayloads()) {
            if (Settings.blockedPayloads.contains(payloadType.name)) {
                KickUtil.kickPlayer(player, event, "Blocked Payload: " + payloadType.name,
                        "&c" + payloadType.name + " is not allowed on this server.");
                return;
            }
        }
    }

    /**
     * Checks for impossibilities between version and brand data.
     *
     * @param event The event to cancel.
     */
    private void checkForImpossibilities(@NotNull CancellableEvent event) {
        for (@NotNull PayloadType payloadType : playerData.getPayloads()) {
            @NotNull ClientVersion playerVersion = playerData.getVersion();
            String versionName = playerVersion.getReleaseName();

            // Checks if the player is using Fabric with an impossible version.
            // Fabric only goes down to 1.14, so any version below that is invalid.
            if (payloadType.name.equals("Fabric") && playerVersion.isOlderThan(ClientVersion.V_1_14)) {
                KickUtil.kickPlayer(player, event, "Impossible Fabric Version: " + versionName,
                        "&cYour Fabric version is not compatible with this server.");
                return;
            }

            // Checks if the player is using LiteLoader with an impossible version.
            // LiteLoader only goes up to 1.12.2, so any version above that is invalid.
            if (payloadType.name.equals("LiteLoader")
                    && playerVersion.isNewerThan(ClientVersion.V_1_12_2)) {
                KickUtil.kickPlayer(player, event, "Impossible LiteLoader Version: " + versionName,
                        "&cYour LiteLoader version is not compatible with this server.");
                return;
            }

            // Checks if the player is using Feather Client with an impossible version.
            if (payloadType.name.equals("Feather Client")) {
                switch (playerVersion) {
                    case HIGHER_THAN_SUPPORTED_VERSIONS:
                    case V_1_21_4:
                    case V_1_21_2:
                    case V_1_12_1:
                    case V_1_20_5:
                    case V_1_20_3:
                    case V_1_20_2:
                    case V_1_20:
                    case V_1_19_4:
                    case V_1_19_3:
                    case V_1_19_1:
                    case V_1_19:
                    case V_1_18_2:
                    case V_1_17_1:
                    case V_1_12_2:
                    case V_1_8:
                        break;

                    default:
                        KickUtil.kickPlayer(player, event, "Impossible Feather Client Version: " + versionName,
                                "&cYour Feather Client version is not compatible with this server.");
                        return;
                }
            }

            // Checks if the player is using PvPLounge Client with an impossible version.
            // PvPLounge client only goes up to 1.8.9, so any version above that is invalid.
            if (payloadType.name.equals("PvPLounge Client")
                    && playerVersion.isNewerThan(ClientVersion.V_1_8)) {
                KickUtil.kickPlayer(player, event, "Impossible PvPLounge Client Version: " + versionName,
                        "&cYour PvPLounge Client version is not compatible with this server.");
                return;
            }

            // Checks if the player is using LabyMod with an impossible version.
            if (payloadType.name.equals("LabyMod")) {
                switch (playerVersion) {
                    case HIGHER_THAN_SUPPORTED_VERSIONS:
                    case V_1_21_4:
                    case V_1_21_2:
                    case V_1_21:
                    case V_1_20_5:
                    case V_1_20_3:
                    case V_1_20_2:
                    case V_1_20:
                    case V_1_19_4:
                    case V_1_19_3:
                    case V_1_19_1:
                    case V_1_18_2:
                    case V_1_17_1:
                    case V_1_16_4:
                    case V_1_12_2:
                    case V_1_8:
                        break;

                    default:
                        KickUtil.kickPlayer(player, event, "Impossible LabyMod Version: " + versionName,
                                "&cYour LabyMod version is not compatible with this server.");
                        return;
                }
            }

            // Checks if the player is using Lunar Client with an impossible version.
            if (payloadType.name.equals("Lunar Client")) {
                switch (playerVersion) {
                    case HIGHER_THAN_SUPPORTED_VERSIONS:
                    case LOWER_THAN_SUPPORTED_VERSIONS:
                    case V_1_21_4:
                    case V_1_21_2:
                    case V_1_21:
                    case V_1_20_5:
                    case V_1_20_3:
                    case V_1_20_2:
                    case V_1_20:
                    case V_1_19_4:
                    case V_1_19_3:
                    case V_1_19_1:
                    case V_1_19:
                    case V_1_18_2:
                    case V_1_18:
                    case V_1_17_1:
                    case V_1_16_4:
                    case V_1_12_2:
                    case V_1_8:
                        break;

                    default:
                        KickUtil.kickPlayer(player, event, "Impossible Lunar Client Version: " + versionName,
                                "&cYour Lunar Client version is not compatible with this server.");
                        return;
                }
            }

            // Checks if the player is using Badlion Client with an impossible version.
            if (payloadType.name.equals("Badlion Client")) {
                switch (playerVersion) {
                    case HIGHER_THAN_SUPPORTED_VERSIONS:
                    case V_1_21_4:
                    case V_1_21_2:
                    case V_1_21:
                    case V_1_20_3:
                    case V_1_20:
                    case V_1_19_4:
                    case V_1_16_4:
                    case V_1_12_2:
                    case V_1_8:
                        break;

                    default:
                        KickUtil.kickPlayer(player, event, "Impossible Badlion Client Version: " + versionName,
                                "&cYour Badlion Client version is not compatible with this server.");
                        return;
                }
            }
        }
    }

    /**
     * Validates and processes a payload by checking it against a list of payload types.
     *
     * @param payloadTypeList The payload type list to check.
     * @param event           The event to cancel.
     * @param data            The data to check.
     */
    private void validateAndProcessPayload(@NotNull Iterable<PayloadType> payloadTypeList,
                                           @NotNull CancellableEvent event,
                                           @NotNull String data,
                                           @NotNull DataType dataType) {
        String playerName = player.getName();
        @NotNull String dataTypeName = dataType.getName();

        // Checks if the data is present in the payload type list.
        for (@NotNull PayloadType payloadType : payloadTypeList) {
            if (payloadType.data.equals(data)) {
                // If the payload is blocked, kick the player.
                if (payloadType.isBlocked()) {
                    KickUtil.kickPlayer(player, event, "Blocked " + dataTypeName + ": " + payloadType.name);
                    return;
                }

                // Adds the payload type to the player's list of sent payloads.
                if (playerData.getPayloads().stream().noneMatch(type -> {
                    // Check if the name and payload type are the same.
                    boolean nameMatches = type.name.equals(payloadType.name);
                    boolean dataMatches = type.dataType == payloadType.dataType;
                    return nameMatches && dataMatches;
                })) {
                    playerData.getPayloads().add(payloadType);

//                    // If the payload is a channel, disable mod functionality.
//                    if (dataType == DataType.CHANNEL) {
//                        disableModFunctionality(playerData, data);
//                    }
                }
                return;
            }
        }

        // Payload was not found in the list; handle as unknown data.
        MessageUtil.messagePlayer(Bukkit.getConsoleSender(), Settings.prefix + " &f" + playerName
                        + " &7sent unknown data to the server. &8(Type: " + dataTypeName + ") (Data: " + data + ")");

        // Prints the unknown data to a randomly generated text file.
        // TODO: Remove this in production.
        FileUtil.printDataToFile(data, "unknown-data-" + System.currentTimeMillis() + ".txt");

        // Cancels the packets to prevent the server from registering the channels.
        // This is needed to prevent the server from crashing / errors in console.
        event.setCancelled(true);
    }

//    /**
//     * Disables the functionality of certain mods through registered channels.a
//     *
//     * @param playerData The player data to disable mod functionality for.
//     */
//    @Contract(pure = true)
//    public static void disableModFunctionality(@NotNull PlayerData playerData,
//                                               @NotNull String channelName) {
//        Player player = playerData.getPlayer();
//
//        switch (channelName) {
//            // Disable the Better Sprinting mod.
//            case "BSM": {
//                byte[] data = new byte[1];
//                data[0] = 1; // Message type
//
//                WrapperPlayServerPluginMessage packet = new WrapperPlayServerPluginMessage(channelName, data);
//                PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
//                MessageUtil.messagePlayer(player, "&cBetter Sprinting functionality has been disabled.");
//                break;
//            }
//
//            default:
//                break;
//        }
//    }
}
