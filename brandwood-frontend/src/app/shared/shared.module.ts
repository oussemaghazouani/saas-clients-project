import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LayoutComponent } from './layout/layout.component';
import { NotificationBellComponent } from './notification-bell/notification-bell.component';

@NgModule({
  declarations: [LayoutComponent, NotificationBellComponent],
  imports: [CommonModule, RouterModule],
  exports: [LayoutComponent, NotificationBellComponent]
})
export class SharedModule {}
