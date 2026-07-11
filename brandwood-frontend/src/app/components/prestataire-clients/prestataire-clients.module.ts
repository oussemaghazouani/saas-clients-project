import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { PrestataireClientsComponent } from './prestataire-clients.component';

const routes: Routes = [{ path: '', component: PrestataireClientsComponent }];

@NgModule({
  declarations: [PrestataireClientsComponent],
  imports: [CommonModule, ReactiveFormsModule, SharedModule, RouterModule.forChild(routes)]
})
export class PrestataireClientsModule {}
