import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { AdminPacksComponent } from './admin-packs.component';

const routes: Routes = [{ path: '', component: AdminPacksComponent }];

@NgModule({
  declarations: [AdminPacksComponent],
  imports: [CommonModule, ReactiveFormsModule, SharedModule, RouterModule.forChild(routes)]
})
export class AdminPacksModule {}
