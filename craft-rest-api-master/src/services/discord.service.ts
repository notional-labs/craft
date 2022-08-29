// Discord Webhook Notifications
import { WebhookClient, MessageEmbed, ColorResolvable } from 'discord.js';

// Env
import { config } from 'dotenv';
config();

//  Load in the webhook client (single channel)
const webhookClient = new WebhookClient({ url: `${process.env.CRAFT_DAO_ESCROW_WEBHOOK_URL}` });

// send the webhooks, ColorResolvable can be a hex color code string
export const sendDiscordWebhook = async (title: string, description: string, fields: {}, color: ColorResolvable) => {
    const embed = new MessageEmbed();

    embed.setTitle(title).setDescription(description).setColor(color);
    for(let key in fields) {
        // console.log(key, fields[key]);
        embed.addField(key, fields[key]);
    }

    webhookClient.send({
        username: 'Craft Economy API Notifications',
        avatarURL: 'https://cdn.discordapp.com/icons/871646581777125417/a_8b2db9405010bee2b7cb831e9d53381c.webp?size=128',
        embeds: [embed],
    });
}