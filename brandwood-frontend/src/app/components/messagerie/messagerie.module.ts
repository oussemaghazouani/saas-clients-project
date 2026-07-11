import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { MessagerieComponent } from './messagerie.component';

const routes: Routes = [{ path: '', component: MessagerieComponent }];

@NgModule({
  declarations: [MessagerieComponent],
  imports: [CommonModule, FormsModule, SharedModule, RouterModule.forChild(routes)]
})
export class MessagerieModule {}
