import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { AdminAbonnementsComponent } from './admin-abonnements.component';

const routes: Routes = [{ path: '', component: AdminAbonnementsComponent }];

@NgModule({
  declarations: [AdminAbonnementsComponent],
  imports: [CommonModule, FormsModule, SharedModule, RouterModule.forChild(routes)]
})
export class AdminAbonnementsModule {}
