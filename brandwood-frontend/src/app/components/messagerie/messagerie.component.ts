import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { Contact, MessagePrive, MessagerieService } from '../../services/messagerie.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-messagerie',
  templateUrl: './messagerie.component.html'
})
export class MessagerieComponent implements OnInit, OnDestroy {

  contacts: Contact[] = [];
  selected: Contact | null = null;
  messages: MessagePrive[] = [];
  nouveau = '';
  loading = false;
  error = '';
  moiId = 0;

  private sub?: Subscription;

  constructor(private svc: MessagerieService, private auth: AuthService) {}

  ngOnInit(): void {
    this.moiId = this.auth.getCurrentUser()?.userId ?? 0;
    this.charger();
    this.svc.connecter();
    this.sub = this.svc.messages$.subscribe(m => this.onMessageRecu(m));
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    this.svc.deconnecter();
  }

  charger(): void {
    this.loading = true;
    this.svc.contacts().subscribe({
      next: c => { this.contacts = c; this.loading = false; },
      error: e => { this.error = e.error?.message || 'Erreur de chargement.'; this.loading = false; }
    });
  }

  ouvrir(c: Contact): void {
    this.selected = c;
    c.nonLus = 0;
    this.svc.fil(c.userId).subscribe({
      next: m => { this.messages = m; this.scrollBas(); },
      error: e => this.error = e.error?.message || 'Conversation introuvable.'
    });
  }

  envoyer(): void {
    const texte = this.nouveau.trim();
    if (!texte || !this.selected) { return; }
    const dest = this.selected.userId;
    this.svc.envoyer(dest, texte).subscribe({
      next: msg => {
        this.nouveau = '';
        this.messages.push(msg);
        this.majApercu(dest, msg);
        this.scrollBas();
      },
      error: e => this.error = e.error?.message || 'Envoi impossible.'
    });
  }

  /** Message recu en temps reel (WebSocket). */
  private onMessageRecu(m: MessagePrive): void {
    const autre = m.expediteurId; // c'est forcement un message recu
    if (this.selected && this.selected.userId === autre) {
      this.messages.push(m);
      this.svc.fil(autre).subscribe(); // marque comme lus cote serveur
      this.scrollBas();
    } else {
      const c = this.contacts.find(x => x.userId === autre);
      if (c) { c.nonLus = (c.nonLus || 0) + 1; }
    }
    this.majApercu(autre, m);
  }

  private majApercu(userId: number, m: MessagePrive): void {
    const c = this.contacts.find(x => x.userId === userId);
    if (c) {
      c.dernierMessage = m.contenu;
      c.dernierMessageDate = m.dateEnvoi;
      // remonte la conversation en haut
      this.contacts = [c, ...this.contacts.filter(x => x.userId !== userId)];
    }
  }

  private scrollBas(): void {
    setTimeout(() => {
      const el = document.getElementById('fil-messages');
      if (el) { el.scrollTop = el.scrollHeight; }
    }, 50);
  }

  estMoi(m: MessagePrive): boolean { return m.expediteurId === this.moiId; }

  roleBadge(r: string): string {
    return { CLIENT: 'badge-available', PRESTATAIRE: 'badge-category', SUPER_ADMIN: 'badge-admin' }[r] || 'badge-pending';
  }

  heure(iso: string | null): string {
    if (!iso) { return ''; }
    const d = new Date(iso);
    return isNaN(d.getTime()) ? '' : d.toLocaleString('fr-FR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' });
  }

  initiales(nom: string): string {
    return nom.split(' ').filter(Boolean).slice(0, 2).map(p => p[0]?.toUpperCase()).join('');
  }
}
