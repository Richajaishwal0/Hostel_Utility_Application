public class FeedbackClient {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java FeedbackClient <feedback_type>");
            System.out.println("feedback_type: good, average, poor");
            System.exit(1);
        }
        
        String feedbackType = args[0].toLowerCase();
        
        if (!feedbackType.equals("good") && !feedbackType.equals("average") && !feedbackType.equals("poor")) {
            System.out.println("Invalid feedback type! Use: good, average, or poor");
            System.exit(1);
        }
        
        // Submit feedback to shared memory
        FeedbackServer.submitFeedback(feedbackType);
        
        // Display current status
        System.out.println("Current status: " + FeedbackServer.getStatus());
    }
}