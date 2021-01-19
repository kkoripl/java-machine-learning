import {Injectable} from "@angular/core";

@Injectable()
export class ApiService {
  constructor() {}

  private apiBase(): string {
    return '/api';
  }

  private handwritingBase(): string {
    return this.apiBase() + '/handwritting';
  }

  private objectDetectionBase(): string {
    return this.apiBase() + '/objectDetection';
  }

  handWritingRecognitionUrl(): string {
    return this.handwritingBase() + '/recognize';
  }

  detectObjectUrl(): string {
    return this.objectDetectionBase() + "/detect";
  }
}
