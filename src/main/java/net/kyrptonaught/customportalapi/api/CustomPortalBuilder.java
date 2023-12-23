package net.kyrptonaught.customportalapi.api;

import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.kyrptonaught.customportalapi.event.PortalIgniteEvent;
import net.kyrptonaught.customportalapi.event.PortalPreIgniteEvent;
import net.kyrptonaught.customportalapi.portal.PortalIgnitionSource;
import net.kyrptonaught.customportalapi.util.ColorUtil;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.kyrptonaught.customportalapi.util.SHOULDTP;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;
import java.util.function.Function;

public class CustomPortalBuilder {
    private final PortalLink portalLink;

    private CustomPortalBuilder(PortalLink portalLink) {
        this.portalLink = portalLink;
    }

    /**
     * Begin the creation of a new Portal
     *
     * @return an instance of CustomPortalBuilder to begin configuring the portal
     */
    public static CustomPortalBuilder beginPortal() {
        return beginPortal(new PortalLink());
    }

    /**
     * Begin the creation of a new Portal, with a custom PortalLink Implementation
     *
     * @return an instance of CustomPortalBuilder to begin configuring the portal
     */
    public static CustomPortalBuilder beginPortal(PortalLink portalLink) {
        return new CustomPortalBuilder(portalLink);
    }

    /**
     * Register the portal when completed.
     * This should be called last, only when you are finished configuring the portal
     *
     * @return the raw PortalLink created from this builder.
     */
    public PortalLink registerPortal() {
        CustomPortalApiRegistry.addPortal(Registries.BLOCK.get(portalLink.block), portalLink);
        return portalLink;
    }

    /**
     * Forcefully register a portal.
     * This bypasses any checks, only use this if you know what you are doing.
     *
     * @return the raw PortalLink created from this builder.
     */
    @Deprecated
    public PortalLink registerPortalForced() {
        CustomPortalApiRegistry.forceAddPortal(Registries.BLOCK.get(portalLink.block), portalLink);
        return portalLink;
    }

    /**
     * Specify the Block Identifier to be used as the Frame
     *
     * @param blockID Block identifier of the portal's frame block
     */
    public CustomPortalBuilder frameBlock(Identifier blockID) {
        portalLink.block = blockID;
        return this;
    }

    /**
     * Specify the Block to be used as the Frame
     *
     * @param block The Block to be used as the portal's frame block
     */
    public CustomPortalBuilder frameBlock(Block block) {
        portalLink.block = Registries.BLOCK.getId(block);
        return this;
    }

    /**
     * Specify the destination for the portal
     *
     * @param dimID Identifier of the Dimension the portal will travel to
     */
    public CustomPortalBuilder destDimID(Identifier dimID) {
        portalLink.dimID = dimID;
        return this;
    }

    /**
     * Specify the color to be used to tint the portal block.
     *
     * @param color Single Color int value used for tinting. See {@link net.minecraft.util.DyeColor}
     */
    public CustomPortalBuilder tintColor(int color) {
        portalLink.colorID = color;
        return this;
    }

    /**
     * Specify the color in RGB to be used to tint the portal block.
     */
    public CustomPortalBuilder tintColor(int r, int g, int b) {
        portalLink.colorID = ColorUtil.getColorFromRGB(r, g, b);
        return this;
    }

    /**
     * This portal will be ignited by water
     */
    public CustomPortalBuilder lightWithWater() {
        portalLink.portalIgnitionSource = PortalIgnitionSource.WATER;
        return this;
    }

    /**
     * This portal will be ignited by an item
     *
     * @param item Item to be used to ignite the portal
     */
    public CustomPortalBuilder lightWithItem(Item item) {
        portalLink.portalIgnitionSource = PortalIgnitionSource.ItemUseSource(item);
        return this;
    }

    /**
     * This portal will be ignited by a fluid.
     *
     * @param fluid Fluid to be used to ignite the portal
     */
    public CustomPortalBuilder lightWithFluid(Fluid fluid) {
        portalLink.portalIgnitionSource = PortalIgnitionSource.FluidSource(fluid);
        return this;
    }

    /**
     * Specify a Custom Ignition Source to be used to ignite the portal. You must manually trigger the ignition yourself.
     */
    public CustomPortalBuilder customIgnitionSource(Identifier customSourceID) {
        portalLink.portalIgnitionSource = PortalIgnitionSource.CustomSource(customSourceID);
        return this;
    }

    /**
     * Specify a Custom Ignition Source to be used to ignite the portal. You must manually trigger the ignition yourself.
     */
    public CustomPortalBuilder customIgnitionSource(PortalIgnitionSource ignitionSource) {
        portalLink.portalIgnitionSource = ignitionSource;
        return this;
    }

    /**
     * Specify the forced size of the portal.
     * Portal will only be ignitable for these exact dimensions
     *
     * @param width  Forced width of portal
     * @param height Forced height of portal
     */
    public CustomPortalBuilder forcedSize(int width, int height) {
        portalLink.forcedWidth = width;
        portalLink.forcedHeight = height;
        return this;
    }

    /**
     * Specify a custom block to be used as the portal block. Block must extend CustomPortalBlock.
     */
    public CustomPortalBuilder customPortalBlock(CustomPortalBlock portalBlock) {
        portalLink.setPortalBlock(portalBlock);
        return this;
    }

    /**
     * Specify the dimension this portal will return you to.
     *
     * @param returnDimID              Identifer of the dimmension the portal will return you to when leaving destination
     * @param onlyIgnitableInReturnDim Should this portal only be ignitable in returnDimID
     */
    public CustomPortalBuilder returnDim(Identifier returnDimID, boolean onlyIgnitableInReturnDim) {
        portalLink.returnDimID = returnDimID;
        portalLink.onlyIgnitableInReturnDim = onlyIgnitableInReturnDim;
        return this;
    }

    /**
     * Specify that this portal can only be ignited in the Overworld.
     * Attempting to light it in other dimensions will fail.
     */
    public CustomPortalBuilder onlyLightInOverworld() {
        portalLink.onlyIgnitableInReturnDim = true;
        return this;
    }

    /**
     * Specify that this is a flat portal (end portal style).
     */
    public CustomPortalBuilder flatPortal() {
        portalLink.portalFrameTester = CustomPortalsMod.FLATPORTAL_FRAMETESTER;
        return this;
    }

    /**
     * Specify a custom portal frame tester to be used.
     */
    public CustomPortalBuilder customFrameTester(Identifier frameTester) {
        portalLink.portalFrameTester = frameTester;
        return this;
    }

    /**
     * Register an event to be called immediately before the specified entity is teleported.
     * The teleportation can be cancelled by returning SHOULDTP.CANCEL_TP
     */
    public CustomPortalBuilder registerBeforeTPEvent(Function<Entity, SHOULDTP> event) {
        portalLink.getBeforeTPEvent().register(event);
        return this;
    }

    /**
     * Register an event to be called after the specified entity is teleported.
     */
    public CustomPortalBuilder registerPostTPEvent(Consumer<Entity> event) {
        portalLink.setPostTPEvent(event);
        return this;
    }

    /**
     * Register an event to be called before a portal is lit.
     * PortalPreIgniteEvent returns true if the portal should be lit, or false if not
     */
    public CustomPortalBuilder registerPreIgniteEvent(PortalPreIgniteEvent event) {
        portalLink.setPortalPreIgniteEvent(event);
        return this;
    }

    /**
     * Register an event to be called after a portal is lit.
     */
    public CustomPortalBuilder registerIgniteEvent(PortalIgniteEvent event) {
        portalLink.setPortalIgniteEvent(event);
        return this;
    }

    /**
     * Sets the Y level range that should be used when searching for a valid location to create a destination portal.
     * By default, this is set to the bottom and top of the world.
     *
     * @param bottomY the lowest Y level to create a portal.
     * @param topY    the highest Y level to create a portal.
     * @see CustomPortalBuilder#setReturnPortalSearchYRange(int, int)
     */
    public CustomPortalBuilder setPortalSearchYRange(int bottomY, int topY) {
        portalLink.portalSearchYBottom = bottomY;
        portalLink.portalSearchYTop = topY;
        return this;
    }

    /**
     * Sets the Y level range that should be used when searching for a valid location to create a return portal.
     * By default, this is set to the bottom and top of the world.
     *
     * @param bottomY the lowest Y level to create a portal.
     * @param topY    the highest Y level to create a portal.
     * @see CustomPortalBuilder#setPortalSearchYRange(int, int)
     */
    public CustomPortalBuilder setReturnPortalSearchYRange(int bottomY, int topY) {
        portalLink.returnPortalSearchYBottom = bottomY;
        portalLink.returnPortalSearchYTop = topY;
        return this;
    }
}
