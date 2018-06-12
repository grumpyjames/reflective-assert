package net.digihippo.reflect;

final class DeepCopyMatchResult
{
    final boolean isDeepCopy;
    final String failureDescription;

    public DeepCopyMatchResult(boolean isDeepCopy, String failureDescription)
    {
        this.isDeepCopy = isDeepCopy;
        this.failureDescription = failureDescription;
    }

    public static DeepCopyMatchResult failure(String failureMessage)
    {
        return new DeepCopyMatchResult(false, failureMessage);
    }

    public static DeepCopyMatchResult success()
    {
        return new DeepCopyMatchResult(true, null);
    }
}
