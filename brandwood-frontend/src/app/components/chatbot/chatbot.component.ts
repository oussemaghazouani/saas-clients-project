import { Component } from '@angular/core';
import { AssistantService } from '../../services/assistant.service';
import { AuthService } from '../../services/auth.service';

interface ChatMsg { from: 'user' | 'bot'; text: string; }

/** Widget chatbot flottant (assistant IA) disponible sur toute l'application une fois connecté. */
@Component({
  selector: 'app-chatbot',
  templateUrl: './chatbot.component.html'
})
export class ChatbotComponent {

  open = false;
  input = '';
  sending = false;
  messages: ChatMsg[] = [
    { from: 'bot', text: "Bonjour 👋 Je suis l'assistant SaaS Client. Posez-moi une question sur vos projets, clients, packs…" }
  ];

  constructor(private assistant: AssistantService, private auth: AuthService) {}

  get loggedIn(): boolean { return this.auth.isLoggedIn(); }

  toggle(): void { this.open = !this.open; }

  send(): void {
    const msg = this.input.trim();
    if (!msg || this.sending) { return; }
    this.messages.push({ from: 'user', text: msg });
    this.input = '';
    this.sending = true;
    this.assistant.chat(msg).subscribe({
      next: r => { this.messages.push({ from: 'bot', text: r.reply }); this.sending = false; },
      error: () => { this.messages.push({ from: 'bot', text: 'Désolé, une erreur est survenue.' }); this.sending = false; }
    });
  }
}
