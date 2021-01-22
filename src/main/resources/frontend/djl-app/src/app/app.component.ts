import {Component, OnInit} from '@angular/core';
import {FileUploaderService} from "./service/file-uploader-service";
import {ApiService} from "./service/api-service";


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'djl-app';
  recognizedText: string;
  probabilities: number[];
  classes: string[];
  imageUrl: string
  fileUploaderService: FileUploaderService;
  apiService: ApiService;

  constructor(fileUploaderService: FileUploaderService,
              apiService: ApiService) {
    this.fileUploaderService = fileUploaderService;
    this.apiService = apiService;
  }

  ngOnInit(): void {
  }

  changeFile(event) {
    this.fileUploaderService.changeFile(event);
    this.fileUploaderService.getDataUrl().then((url: string) => this.imageUrl = url);
  }

  recognize() {
    this.fileUploaderService.upload(this.apiService.handWritingRecognitionUrl())
      .then((response) => {
          this.recognizedText = response.text;
          this.probabilities = response.probabilities;
          this.classes = response.classes;
      })
  }

  detect() {
    this.fileUploaderService.upload(this.apiService.detectObjectUrl())
      .then((response) => {
        this.imageUrl = 'data:image/jpeg;base64,' + response.imgBytes;
        this.probabilities = response.probabilities;
        this.classes = response.classes;
      })
  }

  detectExternal() {
    this.fileUploaderService.upload(this.apiService.detectObjectExternalUrl())
      .then((response) => {
        this.imageUrl = 'data:image/jpeg;base64,' + response.imgBytes;
        this.probabilities = response.probabilities;
        this.classes = response.classes;
      })
  }
}
