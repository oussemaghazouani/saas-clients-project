import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { AdminPrestatairesComponent } from './admin-prestataires.component';

const routes: Routes = [{ path: '', component: AdminPrestatairesComponent }];

@NgModule({
  declarations: [AdminPrestatairesComponent],
  imports: [CommonModule, FormsModule, SharedModule, RouterModule.forChild(routes)]
})
export class AdminPrestatairesModule {}
