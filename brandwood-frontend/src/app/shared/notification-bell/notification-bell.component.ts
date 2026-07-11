import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NotificationItem, NotificationService } from '../../services/notification.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-notification-bell',
  templateUrl: './notification-bell.component.html'
})
export class NotificationBellComponent implements OnInit, OnDestroy {

  open = false;
  notifications: NotificationItem[] = [];
  nonLues = 0;
  private timer: any;

  constructor(private svc: NotificationService, private auth: AuthService, private router: Router) {}

  get loggedIn(): boolean { return this.auth.isLoggedIn(); }

  ngOnInit(): void {
    if (this.loggedIn) {
      this.refreshCount();
      this.timer = setInterval(() => this.refreshCount(), 20000);
    }
  }

  ngOnDestroy(): void { if (this.timer) { clearInterval(this.timer); } }

  refreshCount(): void {
    this.svc.count().subscribe({ next: c => this.nonLues = c.nonLues, error: () => {} });
  }

  toggle(): void {
    this.open = !this.open;
    if (this.open) {
      this.svc.lister().subscribe({
        next: n => { this.notifications = n; this.nonLues = n.filter(x => !x.lu).length; },
        error: () => {}
      });
    }
  }

  clic(n: NotificationItem): void {
    if (!n.lu) {
      this.svc.marquerLu(n.id).subscribe({ next: () => { n.lu = true; this.nonLues = Math.max(0, this.nonLues - 1); } });
    }
    this.open = false;
    if (n.lien) { this.router.navigateByUrl(n.lien); }
  }

  toutLu(): void {
    this.svc.marquerToutLu().subscribe({
      next: () => { this.notifications.forEach(x => x.lu = true); this.nonLues = 0; }
    });
  }
}
