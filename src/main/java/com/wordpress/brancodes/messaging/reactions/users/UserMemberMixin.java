package com.wordpress.brancodes.messaging.reactions.users;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.unions.DefaultGuildChannelUnion;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.dv8tion.jda.api.utils.ImageProxy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class UserMemberMixin implements User, Member {

	User user;
	Member member;

	public boolean isMember() {
		return member != null;
	}

	public UserMemberMixin(User user, Member member) {
		this.user = user;
		this.member = member;
	}

	public UserMemberMixin(User user) {
		this.user = user;
	}

	public UserMemberMixin(Member member) {
		this.member = member;
	}

	public User asUser() {
		return user != null ? user : member.getUser();
	}

	public Member asMember() {
		return member;
	}

	@NotNull
	@Override
	public String getName() {
		return asUser().getName();
	}

	@NotNull
	@Override
	public String getDiscriminator() {
		return asUser().getDiscriminator();
	}

	@Nullable
	@Override
	public String getAvatarId() {
		return asUser().getAvatarId();
	}

	@Nullable
	@Override
	public String getAvatarUrl() {
		return isMember() ? Member.super.getAvatarUrl() : User.super.getAvatarUrl();
	}

	@Nullable
	@Override
	public ImageProxy getAvatar() {
		return isMember() ? Member.super.getAvatar() : User.super.getAvatar();
	}

	@NotNull
	@Override
	public List<Role> getRoles() {
		return asMember().getRoles();
	}

	@Nullable
	@Override
	public Color getColor() {
		return asMember().getColor();
	}

	@Override
	public int getColorRaw() {
		return 0;
	}

	@Override
	public boolean canInteract(@NotNull Member member) {
		return asMember().canInteract(member);
	}

	@Override
	public boolean canInteract(@NotNull Role role) {
		return asMember().canInteract(role);
	}

	@Override
	public boolean canInteract(@NotNull RichCustomEmoji richCustomEmoji) {
		return asMember().canInteract(richCustomEmoji);
	}

	@Override
	public boolean isOwner() {
		return asMember().isOwner();
	}

	@Override
	public boolean isPending() {
		return asMember().isPending();
	}

	@Nullable
	@Override
	public DefaultGuildChannelUnion getDefaultChannel() {
		return asMember().getDefaultChannel();
	}

	@NotNull
	@Override
	public String getDefaultAvatarId() {
		return asUser().getDefaultAvatarId();
	}

	@NotNull
	@Override
	public String getEffectiveAvatarUrl() {
		return isMember() ? Member.super.getEffectiveAvatarUrl() : User.super.getEffectiveAvatarUrl();
	}

	@NotNull
	@Override
	public ImageProxy getEffectiveAvatar() {
		return isMember() ? Member.super.getEffectiveAvatar() : User.super.getEffectiveAvatar();
	}

	@NotNull
	@Override
	public CacheRestAction<Profile> retrieveProfile() {
		return asUser().retrieveProfile();
	}

	@NotNull
	@Override
	public String getAsTag() {
		return asUser().getAsTag();
	}

	@Override
	public boolean hasPrivateChannel() {
		return asUser().hasPrivateChannel();
	}

	@NotNull
	@Override
	public CacheRestAction<PrivateChannel> openPrivateChannel() {
		return asUser().openPrivateChannel();
	}

	@NotNull
	@Override
	public List<Guild> getMutualGuilds() {
		return asUser().getMutualGuilds();
	}

	@Override
	public boolean isBot() {
		return asUser().isBot();
	}

	@Override
	public boolean isSystem() {
		return asUser().isSystem();
	}

	@NotNull
	@Override
	public User getUser() {
		return asMember().getUser();
	}

	@NotNull
	@Override
	public Guild getGuild() {
		return asMember().getGuild();
	}

	@NotNull
	@Override
	public EnumSet<Permission> getPermissions() {
		return asMember().getPermissions();
	}

	@NotNull
	@Override
	public EnumSet<Permission> getPermissions(@NotNull GuildChannel guildChannel) {
		return asMember().getPermissions();
	}

	@NotNull
	@Override
	public EnumSet<Permission> getPermissionsExplicit() {
		return asMember().getPermissionsExplicit();
	}

	@NotNull
	@Override
	public EnumSet<Permission> getPermissionsExplicit(@NotNull GuildChannel guildChannel) {
		return asMember().getPermissionsExplicit();
	}

	@Override
	public boolean hasPermission(@NotNull Permission... permissions) {
		return asMember().hasPermission();
	}

	@Override
	public boolean hasPermission(@NotNull Collection<Permission> collection) {
		return asMember().hasPermission();
	}

	@Override
	public boolean hasPermission(@NotNull GuildChannel guildChannel, @NotNull Permission... permissions) {
		return asMember().hasPermission();
	}

	@Override
	public boolean hasPermission(@NotNull GuildChannel guildChannel, @NotNull Collection<Permission> collection) {
		return asMember().hasPermission();
	}

	@Override
	public boolean canSync(@NotNull IPermissionContainer iPermissionContainer, @NotNull IPermissionContainer iPermissionContainer1) {
		return asMember().canSync(iPermissionContainer1);
	}

	@Override
	public boolean canSync(@NotNull IPermissionContainer iPermissionContainer) {
		return false;
	}

	@NotNull
	@Override
	public JDA getJDA() {
		return asUser().getJDA();
	}

	@NotNull
	@Override
	public OffsetDateTime getTimeJoined() {
		return asMember().getTimeJoined();
	}

	@Override
	public boolean hasTimeJoined() {
		return asMember().hasTimeJoined();
	}

	@Nullable
	@Override
	public OffsetDateTime getTimeBoosted() {
		return asMember().getTimeBoosted();
	}

	@Override
	public boolean isBoosting() {
		return asMember().isBoosting();
	}

	@Nullable
	@Override
	public OffsetDateTime getTimeOutEnd() {
		return asMember().getTimeOutEnd();
	}

	@Nullable
	@Override
	public GuildVoiceState getVoiceState() {
		return asMember().getVoiceState();
	}

	@NotNull
	@Override
	public List<Activity> getActivities() {
		return asMember().getActivities();
	}

	@NotNull
	@Override
	public OnlineStatus getOnlineStatus() {
		return asMember().getOnlineStatus();
	}

	@NotNull
	@Override
	public OnlineStatus getOnlineStatus(@NotNull ClientType clientType) {
		return asMember().getOnlineStatus();
	}

	@NotNull
	@Override
	public EnumSet<ClientType> getActiveClients() {
		return asMember().getActiveClients();
	}

	@Nullable
	@Override
	public String getNickname() {
		return asMember().getNickname();
	}

	@NotNull
	@Override
	public String getEffectiveName() {
		return asMember().getEffectiveName();
	}

	@NotNull
	@Override
	public EnumSet<UserFlag> getFlags() {
		return asUser().getFlags();
	}

	@Override
	public int getFlagsRaw() {
		return asUser().getFlagsRaw();
	}

	@NotNull
	@Override
	public String getAsMention() {
		return asUser().getAsMention();
	}

	@Override
	public long getIdLong() {
		return asUser().getIdLong();
	}

	/**
	 * it should probably just compare the IDs, but User/Member don't
 	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;

		if (o instanceof UserMemberMixin) {
			final UserMemberMixin m = (UserMemberMixin) o;
			return isMember() ? asMember().equals(m.asMember()) : asUser().equals(m.asUser());
		}
		if (o instanceof User) {
			return asUser().equals(o);
		}
		if (o instanceof Member) {
			return isMember() ? asMember().equals(o) : asUser().equals(((Member) o).getUser());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return asMember() != null ? member.hashCode() : user.hashCode();
	}

}
