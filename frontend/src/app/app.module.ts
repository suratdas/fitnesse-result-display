import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { AppComponent } from './app.component';
import {FitnesseResultComponent} from './components/fitnesse/fitnesse-result.component';
import {FitnesseService} from './services/fitnesse-result.service';

@NgModule({
  declarations: [
    AppComponent,
    FitnesseResultComponent],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [FitnesseService],
  bootstrap: [AppComponent]
})
export class AppModule { }
